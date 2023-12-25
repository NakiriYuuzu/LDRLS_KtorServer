package yuuzu.net.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.bson.types.ObjectId
import yuuzu.net.data.model.room.Room
import yuuzu.net.data.model.room.RoomDataSource
import yuuzu.net.data.model.user.Identity
import yuuzu.net.data.model.user.User.Companion.verifyIdentity
import yuuzu.net.data.model.user.UserDataSource
import yuuzu.net.data.request.RoomRequest
import yuuzu.net.data.response.ApiResponse
import yuuzu.net.utils.*
import java.time.Clock


fun Route.room(
    roomDataSource: RoomDataSource,
    userDataSource: UserDataSource
) {
    get("/room") {
        val request = call.request.rawQueryParameters
        val name = request["name"]
        if (name != null) {
            when (val roomResult = roomDataSource.getRoomByName(name)) {
                is Results.Error -> call.respond(HttpStatusCode.BadRequest, ApiResponse(roomResult.message))
                is Results.Success -> call.respond(HttpStatusCode.OK, ApiResponse(roomResult.data, true))
            }
        }
        when (val roomResult = roomDataSource.getRooms()) {
            is Results.Success -> call.respond(HttpStatusCode.OK, ApiResponse(roomResult.data, true))
            is Results.Error -> call.respond(HttpStatusCode.BadRequest, ApiResponse(roomResult.message))
        }
    }
    authenticate {
        post("/room") {
            when (val token = call.verifyJWToken(userDataSource, Identity.ADMIN)) {
                is Results.Success -> {
                    val (request, errorMessage) = call.receiveAndValidate<Room>()
                    if (request == null) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse(errorMessage))
                        return@post
                    }

                    val newRoom: Room = (
                        request.copy(
                            _id = ObjectId.get().toString(),
                            name = request.name,
                            roomSize = request.roomSize,
                            roomType = request.roomType,
                            roomAccess = request.roomAccess,
                            validator = true,
                        )
                    )

                    val wasAcknowledged = roomDataSource.insertRoom(newRoom)
                    wasAcknowledged.let { results ->
                        when (results) {
                            is Results.Success -> {
                                if (results.data) call.respond(HttpStatusCode.OK, ApiResponse("success", true))
                                else call.respond(HttpStatusCode.Conflict, ApiResponse("Add new room failed."))
                            }
                            is Results.Error -> {
                                call.respond(HttpStatusCode.Conflict, ApiResponse(results.message))
                            }
                        }
                    }

                }
                is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(token.message))

            }

        }
        put("/room") {
            when (val token = call.verifyJWToken(userDataSource, Identity.ADMIN)) {
                is Results.Success -> {
                    val (request, errorMessage) = call.receiveAndValidate<Room>()
                    if (request == null) {
                        call.respond(HttpStatusCode.BadRequest, ApiResponse(errorMessage))
                        return@put
                    }

                    when (val room = roomDataSource.getRoomById(request._id)) {
                        is Results.Success -> {
                            if (room.data == null) {
                                call.respond(HttpStatusCode.Conflict, ApiResponse("Room not found."))
                                return@put
                            }
                            val modifyRoom: Room = room.data.copy(
                                name = request.name,
                                roomSize = request.roomSize,
                                roomType = request.roomType,
                                roomAccess = request.roomAccess,
                                validator = request.validator,
                                modifierTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                            )

                            val wasAcknowledged = roomDataSource.updateRoom(modifyRoom)
                            wasAcknowledged.let { results ->
                                when (results) {
                                    is Results.Success -> {
                                        if (results.data) call.respond(
                                            HttpStatusCode.OK, ApiResponse("success", true)
                                        )
                                        else call.respond(HttpStatusCode.Conflict,ApiResponse("Update failed."))
                                    }
                                    is Results.Error -> {
                                        call.respond(HttpStatusCode.Conflict, ApiResponse(results.message))
                                    }
                                }
                            }
                        }
                        is Results.Error -> {
                            call.respond(HttpStatusCode.Conflict, ApiResponse(room.message))
                            return@put
                        }

                    }
                }
                is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(token.message))
            }
        }
        delete("/room") {
            when (val token = call.verifyJWToken(userDataSource, Identity.ADMIN)) {
                is Results.Success -> {
                    val request = kotlin.runCatching { call.receiveNullable<Room>() }.getOrNull() ?: kotlin.run {
                        call.respond(
                            HttpStatusCode.BadRequest, ApiResponse("Please check your request body.")
                        )
                        return@delete
                    }

                    val wasAcknowledged = roomDataSource.disableRoom(request._id)
                    wasAcknowledged.let { results ->
                        when (results) {
                            is Results.Success -> {
                                if (results.data) call.respond(
                                    HttpStatusCode.OK, ApiResponse("success", true)
                                )
                                else call.respond(HttpStatusCode.Conflict, ApiResponse("Delete failed."))
                            }
                            is Results.Error -> {
                                call.respond(HttpStatusCode.Conflict, ApiResponse(results.message))
                            }
                        }
                    }
                }
                is Results.Error -> call.respond(HttpStatusCode.Conflict, ApiResponse(token.message))
            }
        }
    }
}
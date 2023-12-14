# LDRLS Server

## 簡介
這是一個圖書館場地租借系統的後端，使用 Ktor Server 框架實作。

## 功能
- [x] 註冊
- [x] 註銷
- [x] 登入
- [x] 修改帳號資料
- [ ] 新增場地
- [ ] 查詢場地
- [ ] 查詢可租借場地時間
- [ ] 新增租借場地
- [ ] 修改場地
- [ ] 取消租借場地

## 架構
![img.png](documentations/img.png)

## 環境
- Ktor Server
- Mongo DB
- Docker

## 部署
### Docker
```
cp .env.example .env
nano .env                        # 設定你自己的環境變數和mongo db連線
docker-compose up --build -d
```

### Local
```
cp .env.example .env
nano .env                        # 設定你自己的環境變數和mongo db連線
./gradlew run                    # 開發模式
```
### Windows
#### Step 1
打開 `idea` 複製 `.env_sample` 並命名為 `.env`，並設定你自己的環境變數和mongo db連線

#### Step 2
打開 `Settings 中的 plugins` 搜尋 `envfile` 並安裝重啓 `idea`

#### Step 3
打開 `Application.kt` 點擊執行后直接取消，並點擊右上角的 `Edit Configurations` 并且跟著此連接做設定
https://plugins.jetbrains.com/plugin/7861-envfile
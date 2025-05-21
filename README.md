# FinBot-Plus

FinBot-Plus 是一个 SpringAI 应用程序，旨在作为财务跟踪机器人，它利用大型语言模型 (LLMs) 并与 Telegram 集成。用户可以通过简单的消息或上传图片来记录他们的支出和收入，然后使用自然语言查询他们的财务数据。该应用程序支持多种 AI 模型，如 Claude、DeepSeek 和 Gemini，用于处理和分析财务记录。

## 功能特性

* **财务记录**: 通过 Telegram 消息或图片上传轻松记录收入和支出。机器人会智能地提取关键信息，例如金额、日期、类型（支出/收入）和备注。
* **多模型 AI 集成**: 利用 Spring AI 集成不同的主流大型语言模型，包括：
    * Claude
    * DeepSeek
    * Gemini
* **自然语言查询**: 用户可以使用自然语言询问有关其财务记录的问题，机器人将提供汇总的答案和见解。
* **每日定时总结**: 机器人会在每天上午 9:00 自动发送前一天的支出总结到你在 `application.yml` 中配置的 `telegram.userId`。
* **数据库**: 使用 SQLite 作为其数据库来存储财务记录。
<img width="607" alt="image" src="https://github.com/user-attachments/assets/b2218c74-ad16-4747-9c04-33cd1f2edc45" />
<img width="588" alt="image" src="https://github.com/user-attachments/assets/295c6a24-e2d0-4083-a292-4d37c0ad9cc4" />
<img width="592" alt="image" src="https://github.com/user-attachments/assets/19b3d94f-30c9-482f-a4fb-00f20100d781" />

## 如何使用

### 前置条件

* Java 21 或更高版本
* Maven
* 一个 Telegram Bot Token
* 所需 AI 模型的 API 密钥（Anthropic、DeepSeek、Gemini）
* 一个 SOCKS5 代理（例如，本地 Shadowsocks），如果 Telegram 在您所在地区被阻止，用于 Telegram 机器人连接。

### 设置

1.  **克隆仓库**:
    ```bash
    git clone [https://github.com/your-username/FinBot-Plus.git](https://github.com/your-username/FinBot-Plus.git)
    cd FinBot-Plus
    ```

2.  **配置 `application.yml`**:
    打开 `src/main/resources/application.yml` 并填写以下详细信息：
    ```yaml
    telegram:
      userId: YOUR_TELEGRAM_USER_ID # 你的 Telegram 用户ID，用于定时消息
      local: /path/to/save # 文件下载本地保存路径，例如 /Users/username/Downloads/finbot-plus-files
      token: YOUR_TELEGRAM_BOT_TOKEN # 你的 Telegram Bot Token
      proxyPort: YOUR_PROXY_PORT # 你的 SOCKS5 代理端口，例如 1080

    spring:
      datasource:
        url: jdbc:sqlite:/path/to/your/record.db # SQLite 数据库文件路径，例如 jdbc:sqlite:/Users/wanglongjun/Downloads/record.db
      ai:
        deepseek:
          base-url: [https://api.deepseek.com](https://api.deepseek.com) # DeepSeek API 基础URL
        anthropic:
          base-url: [https://api.gptsapi.net](https://api.gptsapi.net) # Anthropic API 基础URL
        gemini:
          base-url: [https://openrouter.ai/api](https://openrouter.ai/api) # Gemini API 基础URL
          model: google/gemini-2.5-flash-preview # Gemini 模型名称
        mcp: # Model Context Protocol (MCP) 相关配置，用于工具调用
          client:
            stdio:
              connections:
                sqliteMcp:
                  command: uv
                  args:
                    - --directory
                    - /path/to/your/servers-2025.4.6/src/sqlite # MCP SQLite 服务路径
                    - run
                    - mcp-server-sqlite
                    - --db-path
                    - /path/to/your/record.db # MCP SQLite 数据库路径，与 datasource.url 保持一致
    ```
    **注意**：
    * `YOUR_TELEGRAM_USER_ID` 可以通过向 `@userinfobot` 发送消息来获取。
    * `YOUR_TELEGRAM_BOT_TOKEN` 可以通过 `@BotFather` 创建机器人后获取。
    * AI 模型的 API 密钥需要设置为系统环境变量，例如 `ANTHROPIC_API_KEY`, `DEEPSEEK_API_KEY`, `GEMINI_API_KEY`。

3.  **构建项目**:
    ```bash
    mvn clean install
    ```

4.  **运行应用程序**:
    ```bash
    java -jar target/FinBot-Plus-0.0.1-SNAPSHOT.jar
    ```
    或者使用 Maven 运行:
    ```bash
    mvn spring-boot:run
    ```

### Telegram 机器人使用

1.  **启动机器人**: 在 Telegram 中找到你的机器人并点击 "Start"。
2.  **记录收支**:
    * 直接发送文本消息，例如 "支出 100 晚餐" 或 "收入 500 工资"。
    * 上传包含交易信息的图片，机器人会尝试从图片中提取信息。
3.  **更新记录**: 如果需要修改已记录的信息，可以使用 `recordNo`（记录号）进行更新。例如，发送 `请把ID为RC202505140004的备注更新为麦当劳。
4.  **查询记录**: 向机器人提问，例如 "总结一下昨天的开支" 或 "我上周在麦当劳花了多少钱？"。
6.  **每日总结**: 机器人会在每天上午 9:00 自动发送前一天的支出总结到你在 `application.yml` 中配置的 `telegram.userId`。

### 数据库结构

项目使用 SQLite 数据库，`record` 表的结构如下:

```sql
create table record
(
    record_no  TEXT not null
        primary key,
    date       TEXT not null,
    amount     TEXT not null,
    type       TEXT not null,
    remark     TEXT,
    sub_remark TEXT,
    created_at TIMESTAMP default CURRENT_TIMESTAMP,
    updated_at TIMESTAMP default CURRENT_TIMESTAMP
);

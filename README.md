# NotifyEveryoneBot

## Overview

This project implements a Telegram bot as an AWS Lambda function using OpenJDK 21. It listens for Telegram updates, processes commands like `/notify` and `@all` to mention all chat administrators in a specific Telegram chat.
The bot tracks user message counts using DynamoDB and handles scheduled or custom events via AWS EventBridge. Communication with Telegram is done through the Telegram Bots Java SDK and OkHttpTelegramClient.
The architecture allows serverless, event-driven handling of Telegram messages with scalable, cloud-native services.

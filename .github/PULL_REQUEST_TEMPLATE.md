## Summary

<!-- 用 1-3 个 bullet 说明：做了什么、为什么。-->

-
-

## Test plan

<!-- 列出验证步骤；至少一条手动验证 + 已跑的自动测试。-->

- [ ] `./gradlew test` 全部通过
- [ ] `./gradlew :app:assembleDebug` 通过
- [ ] 手动验证：
  - [ ] ...

## Checklist

- [ ] 改动遵循 `CLAUDE.md` 与 `CONTRIBUTING.md` 的约定
- [ ] 涉及行为变更已更新 `docs/`（FEATURES / API / ARCHITECTURE 按需）
- [ ] 涉及版本变更已更新 `CHANGELOG.md`
- [ ] 新增依赖已登记到 `gradle/libs.versions.toml`
- [ ] 改动 Room schema 时已升 `HauiDatabase.version`
- [ ] 没有提交 `local.properties` / keystore / API key
- [ ] PR 标题简洁（< 70 字符）

## Screenshots (UI 变更必填)

<!-- 截图或录屏。深色 + 浅色 模式各一张。-->

| 浅色 | 深色 |
| --- | --- |
|     |     |

## Notes

<!-- 设计权衡、已知遗留、follow-up issue 链接等。-->

package ai.tnj.haui.core.data.repository

import ai.tnj.haui.core.data.db.dao.ChatMessageDao
import ai.tnj.haui.core.data.db.dao.SessionSummary
import ai.tnj.haui.core.data.db.entity.ChatMessageEntity
import ai.tnj.haui.core.model.ChatMessage
import ai.tnj.haui.core.model.ChatMessageType
import ai.tnj.haui.core.model.MessageRole
import app.cash.turbine.test
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class ChatHistoryRepositoryTest : StringSpec({

    val dao = mockk<ChatMessageDao>()
    val testDispatcher = UnconfinedTestDispatcher()
    val repository = ChatHistoryRepositoryImpl(dao, testDispatcher)

    beforeTest {
        io.mockk.clearMocks(dao)
    }

    "getSessions should return sessions from dao" {
        val sessions = listOf(
            SessionSummary("s1", 1000L, 2, "user", "bot")
        )
        every { dao.getSessionsFlow() } returns flowOf(sessions)

        repository.getSessions().test {
            awaitItem() shouldBe sessions
            awaitComplete()
        }
    }

    "loadSession should return messages from dao mapped to model" {
        val sessionId = "s1"
        val entities = listOf(
            ChatMessageEntity("m1", sessionId, MessageRole.USER, ChatMessageType.Text, "hello", null, null, null, 1000L)
        )
        every { dao.getBySessionFlow(sessionId) } returns flowOf(entities)

        repository.loadSession(sessionId).test {
            val result = awaitItem()
            result.size shouldBe 1
            result[0].id shouldBe "m1"
            result[0].text shouldBe "hello"
            awaitComplete()
        }
    }

    "upsert should call dao for non-TOOL messages" {
        val sessionId = "s1"
        val message = ChatMessage("m1", MessageRole.USER, ChatMessageType.Text, "hello")
        
        coEvery { dao.upsertWithTimestampCheck(any()) } returns Unit

        repository.upsert(sessionId, message)

        coVerify { dao.upsertWithTimestampCheck(match { it.id == "m1" && it.sessionId == sessionId }) }
    }

    "upsert should not call dao for TOOL messages" {
        val sessionId = "s1"
        val message = ChatMessage("m1", MessageRole.TOOL, ChatMessageType.Text, "executing")

        repository.upsert(sessionId, message)

        coVerify(exactly = 0) { dao.upsertWithTimestampCheck(any()) }
    }

    "upsertAll should filter TOOL messages and call dao" {
        val sessionId = "s1"
        val messages = listOf(
            ChatMessage("m1", MessageRole.USER, ChatMessageType.Text, "hello"),
            ChatMessage("m2", MessageRole.TOOL, ChatMessageType.Text, "executing"),
            ChatMessage("m3", MessageRole.ASSISTANT, ChatMessageType.Text, "hi")
        )

        coEvery { dao.upsertAllWithTimestampCheck(any()) } returns Unit

        repository.upsertAll(sessionId, messages)

        coVerify { 
            dao.upsertAllWithTimestampCheck(match { entities ->
                entities.size == 2 && entities.any { it.id == "m1" } && entities.any { it.id == "m3" }
            }) 
        }
    }

    "deleteSession should call dao" {
        val sessionId = "s1"
        coEvery { dao.deleteBySession(sessionId) } returns Unit

        repository.deleteSession(sessionId)

        coVerify { dao.deleteBySession(sessionId) }
    }
})

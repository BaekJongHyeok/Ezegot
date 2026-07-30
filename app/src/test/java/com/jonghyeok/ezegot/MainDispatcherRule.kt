package com.jonghyeok.ezegot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * ViewModel 테스트용 Main 디스패처 교체 규칙.
 *
 * viewModelScope는 Dispatchers.Main을 쓰는데 JVM 단위 테스트에는 메인 루퍼가 없다.
 * 기본값으로 UnconfinedTestDispatcher를 써서 launch가 즉시 실행되게 하면,
 * 테스트에서 별도 advance 없이 결과 상태를 바로 검증할 수 있다.
 */
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

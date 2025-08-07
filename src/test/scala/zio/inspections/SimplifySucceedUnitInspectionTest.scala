package zio.inspections

import zio.intellij.inspections.simplifications.SimplifySucceedUnitInspection

abstract class SimplifySucceedUnitInspectionTest extends ZSimplifyInspectionTest[SimplifySucceedUnitInspection] {
  override protected val hint = s"Replace with ZIO.unit"

  def test_succeed_Unit(): Unit = {
    z(s"${START}ZIO.succeed(())$END").assertHighlighted()
    val text   = z("ZIO.succeed(())")
    val result = z("ZIO.unit")
    testQuickFix(text, result, hint)
  }

  def test_succeed_Unit_type_no_highlight(): Unit =
    z(s"${START}ZIO.succeed { val x = 1; () }$END").assertNotHighlighted()

  def test_succeed_Unit_expected_type_no_highlight(): Unit =
    z(s"val foo: UIO[Unit] = ${START}ZIO.succeed(1)$END").assertNotHighlighted()

}

class SimplifySucceedUnitInspectionTestZIO2 extends SimplifySucceedUnitInspectionTest {
  def test_attempt_Unit(): Unit = {
    z(s"${START}ZIO.attempt(())$END").assertHighlighted()
    val text   = z("ZIO.attempt(())")
    val result = z("ZIO.unit")
    testQuickFix(text, result, hint)
  }

}

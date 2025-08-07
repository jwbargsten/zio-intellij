package zio.inspections

import zio.intellij.inspections.simplifications.SimplifySucceedOptionInspection

abstract class SimplifyOptionInspectionTest(s: String)
    extends ZSimplifyInspectionTest[SimplifySucceedOptionInspection] {
  override protected val hint = s"Replace with $s"
}

abstract class SucceedNoneInspectionTest extends SimplifyOptionInspectionTest("ZIO.none") {

  def test_succeed_None(): Unit = {
    z(s"${START}ZIO.succeed(None)$END").assertHighlighted()
    val text   = z("ZIO.succeed(None)")
    val result = z("ZIO.none")
    testQuickFix(text, result, hint)
  }

  def test_succeed_None_type_no_highlight(): Unit =
    z(s"${START}ZIO.succeed { val x = 1; None }$END").assertNotHighlighted()

}

class SucceedNoneInspectionTestZIO2 extends SucceedNoneInspectionTest {
  def test_attempt_None(): Unit = {
    z(s"${START}ZIO.attempt(None)$END").assertHighlighted()
    val text   = z("ZIO.attempt(None)")
    val result = z("ZIO.none")
    testQuickFix(text, result, hint)
  }
}

abstract class SucceedSomeInspectionTest extends SimplifyOptionInspectionTest("ZIO.some") {

  def test_succeed_Some(): Unit = {
    z(s"${START}ZIO.succeed(Some(a))$END").assertHighlighted()
    val text   = z("ZIO.succeed(Some(a))")
    val result = z("ZIO.some(a)")
    testQuickFix(text, result, hint)
  }

  def test_block_succeed_Some(): Unit = {
    z {
      s"""${START}ZIO.succeed {
         |  Some {
         |    a
         |    a
         |    a
         |  }
         |}$END""".stripMargin
    }.assertHighlighted()
    val text = z {
      """ZIO.succeed {
        |  Some {
        |    a
        |    a
        |    a
        |  }
        |}""".stripMargin
    }
    val result = z {
      """ZIO.some {
        |  a
        |  a
        |  a
        |}""".stripMargin
    }
    testQuickFix(text, result, hint)
  }

}

class SucceedSomeInspectionTestZIO2 extends SucceedSomeInspectionTest

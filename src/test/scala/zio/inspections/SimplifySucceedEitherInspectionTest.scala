package zio.inspections

import zio.intellij.inspections.simplifications.SimplifySucceedEitherInspection

abstract class SimplifySucceedEitherInspectionTest(s: String)
    extends ZSimplifyInspectionTest[SimplifySucceedEitherInspection] {
  override protected val hint = s"Replace with $s"
}

abstract class SucceedLeftInspectionTest extends SimplifySucceedEitherInspectionTest("ZIO.left") {

  def test_succeed_Left(): Unit = {
    z(s"${START}ZIO.succeed(Left(a))$END").assertHighlighted()
    val text   = z("ZIO.succeed(Left(a))")
    val result = z("ZIO.left(a)")
    testQuickFix(text, result, hint)
  }

  def test_block_succeed_Left(): Unit = {
    z {
      s"""${START}ZIO.succeed {
         |  Left {
         |    a
         |    a
         |    a
         |  }
         |}$END""".stripMargin
    }.assertHighlighted()
    val text = z {
      """ZIO.succeed {
        |  Left {
        |    a
        |    a
        |    a
        |  }
        |}""".stripMargin
    }
    val result = z {
      """ZIO.left {
        |  a
        |  a
        |  a
        |}""".stripMargin
    }
    testQuickFix(text, result, hint)
  }

  def test_succeed_util_Left(): Unit = {
    z(s"${START}ZIO.succeed(util.Left(a))$END").assertHighlighted()
    val text   = z("ZIO.succeed(util.Left(a))")
    val result = z("ZIO.left(a)")
    testQuickFix(text, result, hint)
  }

  def test_block_succeed_util_Left(): Unit = {
    z {
      s"""${START}ZIO.succeed {
         |  util.Left {
         |    a
         |    a
         |    a
         |  }
         |}$END""".stripMargin
    }.assertHighlighted()
    val text = z {
      """ZIO.succeed {
        | util.Left {
        |    a
        |    a
        |    a
        |  }
        |}""".stripMargin
    }
    val result = z {
      """ZIO.left {
        |  a
        |  a
        |  a
        |}""".stripMargin
    }
    testQuickFix(text, result, hint)
  }

}

class SucceedLeftInspectionTestZIO2 extends SucceedLeftInspectionTest

abstract class SucceedRightInspectionTest extends SimplifySucceedEitherInspectionTest("ZIO.right") {

  def test_succeed_Right(): Unit = {
    z(s"${START}ZIO.succeed(Right(a))$END").assertHighlighted()
    val text   = z("ZIO.succeed(Right(a))")
    val result = z("ZIO.right(a)")
    testQuickFix(text, result, hint)
  }

  def test_block_succeed_Right(): Unit = {
    z {
      s"""${START}ZIO.succeed {
         |  Right {
         |    a
         |    a
         |    a
         |  }
         |}$END""".stripMargin
    }.assertHighlighted()
    val text = z {
      """ZIO.succeed {
        |  Right {
        |    a
        |    a
        |    a
        |  }
        |}""".stripMargin
    }
    val result = z {
      """ZIO.right {
        |  a
        |  a
        |  a
        |}""".stripMargin
    }
    testQuickFix(text, result, hint)
  }

  def test_succeed_util_Right(): Unit = {
    z(s"${START}ZIO.succeed(util.Right(a))$END").assertHighlighted()
    val text   = z("ZIO.succeed(util.Right(a))")
    val result = z("ZIO.right(a)")
    testQuickFix(text, result, hint)
  }

  def test_block_succeed_util_Right(): Unit = {
    z {
      s"""${START}ZIO.succeed {
         |  util.Right {
         |    a
         |    a
         |    a
         |  }
         |}$END""".stripMargin
    }.assertHighlighted()
    val text = z {
      """ZIO.succeed {
        | util.Right {
        |    a
        |    a
        |    a
        |  }
        |}""".stripMargin
    }
    val result = z {
      """ZIO.right {
        |  a
        |  a
        |  a
        |}""".stripMargin
    }
    testQuickFix(text, result, hint)
  }
}

class SucceedRightInspectionTestZIO2 extends SucceedRightInspectionTest

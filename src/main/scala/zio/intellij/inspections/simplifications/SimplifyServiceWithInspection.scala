package zio.intellij.inspections.simplifications

import org.jetbrains.plugins.scala.codeInspection.collections.{Qualified, Simplification, SimplificationType}
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction
import org.jetbrains.plugins.scala.lang.psi.types.result.Typeable
import org.jetbrains.plugins.scala.lang.psi.types.{ScType, TypePresentationContext}
import org.jetbrains.plugins.scala.lang.refactoring.ScTypePresentationExt
import zio.intellij.inspections._
import zio.intellij.inspections.hasMethods.`.get`
import zio.intellij.inspections.zioMethods._
import zio.intellij.utils.StringUtils.ScExpressionExt
import zio.intellij.utils.TypeCheckUtils.`ZIO[R, E, A]`
import zio.intellij.utils._
import zio.intellij.utils.types.ZioType

class SimplifyServiceWithInspection
    extends ZInspection(
      ServiceWithSimplificationTypeZIO1,
      ServiceWithSimplificationTypeZIO2,
      ServiceWithZIOSimplificationTypeZIO2
    ) {
  override protected def isAvailable(zioVersion: ZioVersion): Boolean = zioVersion >= ZioVersion.ZIO.`1.0.6`
}

abstract class ServiceWithSimplificationType extends SimplificationType {

  protected def needsMoreEnv(func: ScExpression, typeArg: ScType): Boolean =
    returnType(func) match {
      case Some(`ZIO[R, E, A]`(r, _, _)) =>
        val outer = split(typeArg).filterNot(_.isAny).map(_.canonicalText).toSet
        val inner = split(r).filterNot(_.isAny).map(_.canonicalText).toSet
        !inner.subsetOf(outer)
      case _ => false
    }

  private def returnType(func: ScExpression): Option[ScType] =
    (func match {
      // zio.flatMap(foo)
      case ScReferenceExpression(f: ScFunction) => f.returnType
      // zio.flatMap(el => foo(el))
      case lambda(_, res) => res.getTypeIgnoreBaseType
      // zio.flatMap(foo(_))
      case expr => expr.getNonValueType(fromUnderscore = true)
    }).toOption

}

object ServiceWithSimplificationTypeZIO1 extends ServiceWithSimplificationType {
  override def hint: String = "Replace with ZIO.serviceWith"

  private def replacement(
    zioType: ZioType,
    expr: ScExpression,
    ref: ScExpression,
    typeArg: Option[ScType]
  )(implicit
    ctx: TypePresentationContext = TypePresentationContext(expr)
  ): Option[Simplification] = {
    val tpe = typeArg.fold("")(t => s"[${t.codeText}]")

    val refText = ref match {
      case f: ScFunctionExpr => LambdaUtils.lambdaToUnderscore(f).getWrappedText
      case _                 => ref.getWrappedText
    }

    val fnCall = refText.replace("_.get.", "_.")

    val replacement = replace(expr)
      .withText(s"${zioType.name}.serviceWith$tpe$fnCall")
      .highlightFrom(expr)

    Some(replacement)
  }

  override def getSimplification(expr: ScExpression): Option[Simplification] = None

  object InvokedExpression {

    /**
     * 1. _.get.foo is a reference expression, qualified with _.get (or x => x.get)
     * 2. _.get.foo(param) is a method call with an inner reference expr (1)
     * 3. x => x.get.foo is a function expression with an inner reference expr (1)
     * 4. { _.get.foo } is a block with an inner reference expr (1)
     */
    def unapply(expr: ScExpression): Option[ScExpression] = expr match {
      case ref @ ScReferenceExpression.withQualifier(`.get`(_) | lambda(_, `.get`(_))) => Some(ref)
      case mc @ ScMethodCall(InvokedExpression(_), _)                                  => Some(mc)
      case fn @ ScFunctionExpr(_, Some(InvokedExpression(_)))                          => Some(fn)
      case ScBlock(foo @ InvokedExpression(_))                                         => Some(foo)
      case _                                                                           => None
    }
  }
}

abstract class ServiceWithZIO2SimplificationTypeBase(serviceWith: String, mapLike: Qualified)
    extends ServiceWithSimplificationType {
  override val hint: String = s"Replace with ZIO.$serviceWith"

  override def getSimplification(expr: ScExpression): Option[Simplification] = {
    if (!expr.isZio2) return None

    expr match {
      case ScGenericCall(`ZIO.service`(_, _), Seq(Typeable(typeArg))) `mapLike` func if !needsMoreEnv(func, typeArg) =>
        replacement(expr, func, typeArg)
      case _ => None
    }
  }

  private def replacement(expr: ScExpression, func: ScExpression, typeArg: ScType)(implicit
    ctx: TypePresentationContext = TypePresentationContext(expr)
  ): Option[Simplification] = {
    val typeArgument = s"[${typeArg.codeText}]"
    val function     = func.getWrappedText
    val replacement  = replace(expr).withText(s"ZIO.$serviceWith$typeArgument$function").highlightFrom(expr)
    Some(replacement)
  }

}

object ServiceWithSimplificationTypeZIO2 extends ServiceWithZIO2SimplificationTypeBase("serviceWith", `.map`) {
  override protected def needsMoreEnv(func: ScExpression, typeArg: ScType): Boolean = false
}

object ServiceWithZIOSimplificationTypeZIO2 extends ServiceWithZIO2SimplificationTypeBase("serviceWithZIO", `.flatMap`)

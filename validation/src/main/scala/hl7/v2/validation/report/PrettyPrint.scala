package hl7.v2.validation.report

import expression.AsString.{expression => exp}
import expression.EvalResult.{Trace, Reason}
import hl7.v2.instance.{Element, Location}
import hl7.v2.validation.content.Predicate

object PrettyPrint {

  def prettyPrint(r: Report) {
    println(s"\n\n########  Structure check: ${r.structure.size} problem(s) detected.")
    r.structure.reverse foreach { e => println(asString(e)) }

    println(s"\n\n########  Content check: ${r.content.size} problem(s) detected.")
    r.content foreach { e => println(asString(e)) }

    println(s"\n\n########  Value set check: ${r.vs.size} problem(s) detected.")
    r.vs foreach { e => println(asString(e)) }

    println("\n")
  }

  private def asString(e: Entry): String = e match {
    case x: RUsage   => usage(x)
    case x: REUsage  => usage(x)
    case x: XUsage   => usage(x)
    case x: WUsage   => usage(x)
    case x: MinCard  => cardinality(x)
    case x: MaxCard  => cardinality(x)
    case x: Length   => length(x)
    case x: Format   => format(x)
    case x: Extra    => extra(x)
    case x: UnescapedSeparators => unescapedSep(x)

    case x: Success   => success(x)
    case x: Failure   => failure(x)
    case x: SpecError => specErr(x)
    case x: InvalidLines => invalid(x)
    case x: UnexpectedLines => unexpected(x)
    case x: PredicateSuccess   => success(x)
    case x: PredicateFailure   => failure(x)
    case x: PredicateSpecError => specErr(x)

    case x: EVS => evs(x)
    case x: PVS => pvs(x)
    case x: CodeNotFound => codeNotFound(x)
    case x: VSNotFound   => vsNotFound(x)
    case x: EmptyVS      => emptyVS(x)
    case x: VSSpecError  => vsSpecErr(x)
    case x: VSError      => vsErr(x)
    case x: CodedElem    => codedElem(x)
    case x: NoVal        => noVal(x)
  }

  //private def loc(l: Location) = f"[${l.line}%03d, ${l.column}%03d]\t${l.path}(${l.desc})"
  private def loc(l: Location) = f"[${l.line}%03d, ${l.column}%03d]"

  // Usage problems
  private def usage(e: RUsage) = s"${loc(e.location)}\t$e"

  private def usage(e: REUsage) = s"${loc(e.location)}\t$e"

  private def usage(e: XUsage) = s"${loc(e.location)}\t$e"

  private def usage(e: WUsage) = s"${loc(e.location)}\t$e"

  // Cardinality problems
  private def cardinality(e: MinCard) = {
    val expectation = s"Expected ${e.range}, found ${e.instance} repetitions"
    s"${loc(e.location)}\tMinimum cardinality violated. $expectation"
  }

  private def cardinality(e: MaxCard) = {
    val expectation = s"Expected ${e.range}, found ${e.instance} repetitions"
    s"${loc(e.location)}\tMaximum cardinality violated. $expectation"
  }

  private def length(e: Length) = {
    val expectation = s"Expected ${e.range}, found ${e.value.length} => (${e.value})"
    s"${loc(e.location)}\tLength violated. $expectation"
  }

  private def format(e: Format) = s"${loc(e.location)}\t${e.details}"

  private def extra(e: Extra) = s"${loc(e.location)} Extra children"

  private def unescapedSep(e: UnescapedSeparators) =
    s"${loc(e.location)}\tUnescaped separators in primitive element."

  private def invalid(e: InvalidLines) = {
    val ls = e.list.map(l => s"[line=${l.number}, column=1] : ${l.content}")
    s"### Invalid Lines: ${ ls.mkString("\n\t","\n\t", "\n") }"
  }

  private def unexpected(e: UnexpectedLines) = {
    val ls = e.list.map(l => s"[line=${l.number}, column=1] : ${l.content}")
    s"### Unexpected Lines: ${ ls.mkString("\n\t","\n\t", "\n") }"
  }

  //============================================================================
  // Value Set
  //============================================================================

  private def evs(e: EVS)                   = s"${loc(e.location)}\t${e.toString.take(90)}"
  private def pvs(e: PVS)                   = s"${loc(e.location)}\t${e.toString.take(90)}"
  private def codeNotFound(e: CodeNotFound) = s"${loc(e.location)}\t${e.toString.take(90)}"
  private def vsNotFound(e: VSNotFound)     = s"${loc(e.location)}\t${e.toString.take(90)}"
  private def emptyVS(e: EmptyVS)           = s"${loc(e.location)}\t${e.toString.take(90)}"
  private def vsSpecErr(e: VSSpecError)     = s"${loc(e.location)}\t${e.toString.take(90)}"
  private def vsErr(e: VSError)             = s"${loc(e.location)}\t${e.toString.take(90)}"
  private def codedElem(e: CodedElem)       = s"${loc(e.location)}\t${e.toString.take(90)}"
  private def noVal(e: NoVal)               = s"${loc(e.location)}\t${e.toString.take(90)}"


  //============================================================================
  // Content
  //============================================================================

  private def predicate(p: Predicate) = {
    s"${p.description} Target: ${p.target} C(${p.trueUsage}/${p.falseUsage})"
  }
  private def success(e: PredicateSuccess) = s"[PSuccess] ${predicate(e.predicate)}\n"

  private def failure(e: PredicateFailure) = {

    def violation(x: UsageEntry) =  x match {
      case x:RUsage => usage(x)
      case x:XUsage => usage(x)
      case x        => throw new Exception(s"Invalid predicate violation '$x'")
    }
    val violations = (e.violations map violation).mkString("", "\t\t", "\n")
    s"[PFailure] ${predicate(e.predicate)} \n $violations\n"
  }

  private def specErr(e: PredicateSpecError) = {
    val rs = e.reasons.map( r => s"\t\t${reason(r)}" ).mkString("\n")
    s"[PSpec Error] ${predicate(e.predicate)} \n $rs\n"
  }

  private def success(e: Success) = s"[Success] ${loc(e.context.location)} ${
    e.constraint.id.getOrElse("")} - ${ exp(e.constraint.assertion, e.context) }\n"

  private def failure(e: Failure) = s"[Failure] ${loc(e.context.location)} ${
    e.constraint.id.getOrElse("")} - ${ exp(e.constraint.assertion, e.context)
    } \n ${stack(e.context, e.stack, "\t")}\n"

  private def specErr(e: SpecError) = {
    val details = trace(e.context, e.trace, "\t")
    s"[Spec Error] ${loc(e.context.location)} ${e.constraint.id.getOrElse("")
      } - ${exp(e.constraint.assertion, e.context)} \n $details\n"
  }

  private def stack(c: Element, l: List[Trace], tab: String)=
    Stream.from(1).zip(l).map( t => s"${trace(c, t._2, tab * t._1)}").mkString("\n")

  private def trace(c: Element, t: Trace, tab: String) = {
    val rs = t.reasons.map( r => s"$tab\t${reason(r)}" ).mkString("\n")
    s"$tab[Failed] ${exp(t.expression, c)} \n $rs "
  }

  private def reason(r: Reason) =
    s"Reason: [${r.location.line}, ${r.location.column}] ${r.message}"
}
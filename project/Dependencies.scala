import sbt._

object Dependencies {

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  val `xml-util`    = "gov.nist"                       %%  "xml-util"            %  "1.0.0"

  val junit         = "junit"                          %   "junit"               %  "4.11"
  val spec2         = "org.specs2"                     %%  "specs2"              %  "2.3.11"
  val scalaCheck    = "org.scalacheck"                 %%  "scalacheck"          %  "1.11.3" 
}
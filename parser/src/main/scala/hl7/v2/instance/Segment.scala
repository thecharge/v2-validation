package hl7.v2.instance

import hl7.v2.profile.{SegmentRef => SM, Field => FM}

/**
  * Class representing a segment
  */
case class Segment (
    model: SM,
    location: Location,
    instance: Int,
    children: List[Field],
    hasExtra: Boolean
) extends SegOrGroup

/**
  * Segment companion object
  */
object Segment extends EscapeSeqHandler {

  /**
    * Creates and returns a segment
    * @param m - The segment model
    * @param v - The value as string
    * @param i - The instance number
    * @param l - The line number
    * @return A segment
    */
  def apply(m: SM, v: String, i: Int, l: Int)
           (implicit s: Separators): Segment = {
    require( isValid( s.fs, v ), s"Invalid segment instance '$v'" )
    val name = m.ref.name
    require(name == v.take(3), s"Invalid segment name. Expected: '$name', Found: '$v'")
    val loc = Location(m.ref.desc, s"$name[$i]", l, 1)
    val fml = m.ref.fields
    val vs  = split( s.fs, v drop 4 , 5)
    val (hasExtra, lfs) =
      if( v startsWith "MSH" ) (vs.size > fml.size - 1) -> mshFields(fml, vs, loc)
      else (vs.size > fml.size) -> fields( fml, vs, loc )
    Segment(m, loc, i, lfs.flatten, hasExtra)
  }

  /**
    * Creates abd returns a list of field
    * @param fml - The list of field models
    * @param vs  - The value and column array
    * @param l   - The parent location
    * @return A list of field
    */
  private def fields( fml: List[FM], vs: Array[(Int, String)], l: Location)
                    (implicit s: Separators) =
    fml zip vs map { t => repetitions( t._1, t._2, l ) }

  private def mshFields( fml: List[FM], vs: Array[(Int, String)], l: Location )
                       (implicit s: Separators) = {
    val `MSH.1` = field(l, fml.head, escape( s"${s.fs}" ), 1, 4)
    val `MSH.2` = field(l, fml.tail.head, escape( vs(0)._2 ), 1, 5)
    val _fields = fields(fml.tail.tail, vs drop 1 , l)
    `MSH.1`.toList  :: `MSH.2`.toList :: _fields
  }

  /**
    * Creates and returns a field
    * @param l - The parent location
    * @param m - The field model
    * @param v - The value as string
    * @param i - The instance(repetition) number
    * @param c - The column
    * @return A field
    */
  private def field(l: Location, m: FM, v: String, i: Int, c: Int)
                   (implicit s: Separators) =
    DataElement(m, v, location(l, m.name, m.req.position, i, c), i)

  /**
    * Creates and returns a list representing a repetition of a field
    * @param m - The field model
    * @param t - The column and value
    * @param l - The parent location
    * @return A list representing a repetition of a field
    */
  private def repetitions( m: FM, t: (Int, String), l: Location)
                         (implicit s: Separators): List[Field] = {
    val vs = split(s.rs, t._2, t._1)
    val r = vs.toList.zipWithIndex map { tt =>
      val( (col, v), ins) = tt
      field(l, m, v, ins + 1, col)
    }
    r.flatten
  }

  /**
    * Regular expression for matching valid segment instance
    */
  private def segFormat(fs: Char) = s"[A-Z]{2}[A-Z0-9](?:\\Q$fs\\E.*)*".r

  /**
    * Returns true if s is a valid segment instance
    * @param s - The segment as string
    * @return True if s is a valid segment instance
    */
  private def isValid( fs: Char, s: String ) = segFormat(fs).pattern.matcher( s ).matches

  /**
    * Creates and returns a location from the parent location
    * @param l - The parent location
    * @param d - The current location description
    * @param p - The current location position
    * @param i - The current location instance
    * @param c - The current location column
    * @return A location
    */
  private def location(l: Location, d: String,  p: Int, i: Int, c: Int) =
    l.copy( desc=d, path=s"${l.path}.$p[$i]", column = c )
}

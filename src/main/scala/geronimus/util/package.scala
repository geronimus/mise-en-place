package geronimus

/** A collection of miscellaneous utility functions. */
package object util {

  /** An SQL-style `COALESCE` operation that can take any number of inputs, and
    * returns the first one that is not null. It will return `null` if you input
    * only `null` arguments.
    *
    * If your first input to this function is a dynamically typed variable (eg,
    * its type is not declared), and it could be `null`, then you may want to
    * parameterize your call to this function with the expected result type.
    *
    * eg:
    *
    * {{{
    * val myVal = maybeNull()
    * val safeString = coalesce[ String ]( myVal, "" )
    * }}}
    *
    * Otherwise, the result could be of type Any, which may cause a type mismatch.
    */
  def coalesce[ T ]( inputs : T* ) : T =
    if ( inputs == Nil ) null.asInstanceOf[ T ]
    else if ( inputs.tail == Nil ) inputs.head
    else if ( inputs.head == null ) coalesce( inputs.tail: _* )
    else inputs.head

  /** Provides a SHA-1 hash of a byte array, represented as a hexadecimal
    * String. Used to determine value identity for arbitrary-length data.
    * NOT TO BE USED FOR CRYPTOGRAPHY.
    * @param bytes It is your responsibility to convert your input data to a
    *   byte array. (eg, `"my text".getBytes( "UTF-8" )` )
    */
  def sha1( bytes : Array[ Byte ] ) =
    java.security.MessageDigest.getInstance( "SHA-1" )
      .digest( coalesce( bytes, Array.empty[ Byte ] ) )
      .map(
        byte => String.format( "%02x", byte )  
      )
      .mkString
}


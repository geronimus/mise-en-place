package geronimus.cli

import org.scalatest.funspec.AnyFunSpec

class ArgSetUsageSpec extends AnyFunSpec {
    
  val isValidPathPattern = ( path : String ) =>
    ( path matches "^\\.?(/[0-9a-zA-Z\\-\\.]+)+$" ) ||
      ( path matches "^([a-zA-Z]:|\\.)(\\\\[0-9a-zA-Z\\-\\.]+)+$" )

  val configPath = Setting.define(
    name = "--configPath",
    description = "The path to the config file.",
    aliases = Set( "--conf", "-c" ),
    validators = Set( isValidPathPattern )
  )

  val outputPath = Setting.define(
    name = "--outputPath",
    description = "The path to the output file. (If it exists it will be overwritten.)",
    aliases = Set( "--out", "-o" ),
    validators = Set( isValidPathPattern )
  )

  val help = Setting.define(
    name = "--help",
    description = "Displays the help text.",
    aliases = Set( "-h", "/?" )
  )

  val settings = Vector( configPath, outputPath, help )
  val paramSet = ParamSet.define( settings )
  val noArgs = paramSet.parseArgs( Array.empty[ String ] )
  val negativeResult = ArgResult(
    found = false,
    valued = false,
    valid = Vector.empty,
    invalid = Vector.empty
  )

  describe( "ArgSet - Primary Usage" ) {
  
    it( "Even an empty one contains all of its settings." ) {

      assert( noArgs.isEmpty )

      val emptyResultMap = (
        for ( setting <- settings )
        yield ( setting.name, negativeResult )
      ).toMap
      assert( noArgs.results == emptyResultMap )

      val settingsMap = (
        for ( setting <- settings )
        yield ( setting.name, setting )
      ).toMap
      assert( noArgs.keySet == settingsMap.keySet )
      assert( noArgs.params == settingsMap )
    }

    it( "Will tell you whether or not it's got a parameter defined." ) {

      assert( noArgs.hasParam( configPath.name ) )
      assert( !noArgs.hasParam( "--verbose" ) )
    }

    it(
      "If you insist on querying an undefined parameter, " +
        "you get an ordinary empty message."
    ) {
      
      assert( noArgs( "--verbose" ) == negativeResult )
      assert( noArgs.result( "--verbose" ) == negativeResult )
    }

    it( "You can use it to detect flags." ) {

      val helpFlag = paramSet.parseArgs( Array( "--help" ) )
      val helpFlagResult = helpFlag( "--help" )
      assert( helpFlagResult.found )
      assert( !helpFlagResult.valued )
      assert( helpFlagResult.valid.isEmpty )
      assert( helpFlagResult.invalid.isEmpty )
    }

    it( "You can use it to detect invalid args." ) {
    
      val invalidConfig = paramSet.parseArgs( Array( "--configPath", "here" ) )
      val invalidResult = invalidConfig( "--configPath" )
      assert( invalidResult.found )
      assert( invalidResult.valued )
      assert( invalidResult.valid.isEmpty )
      assert( invalidResult.invalid == Seq( "here" ) )
    }

    it( "You can use it to detect multiple invalid args, in the right order." ) {
    
      val invalidConfig = paramSet.parseArgs(
        Array( "--configPath", "here", "there" )
      )
      val invalidResult = invalidConfig( "--configPath" )
      assert( invalidResult.found )
      assert( invalidResult.valued )
      assert( invalidResult.valid.isEmpty )
      assert( invalidResult.invalid == Seq( "here", "there" ) )
    }

    it( "You can use it to detect multiple valid and invalid args." ) {
    
      val args = Array(
        "/etc/configs.ini",
        "C:\\ProgramData\\configs.ini",
        "and here"
      )
      val argSet = paramSet.parseArgs( Array( "--configPath" ) ++ args )
      val configResult = argSet( "--configPath" )
      assert( configResult.found )
      assert( configResult.valued )
      assert( configResult.valid == ( args take 2 ).toVector )
      assert( configResult.invalid == args.tail.tail.toVector )
    }

    it( "Nets args, when used as intended." ) {
    
      val argMap = Map(
        "--configPath" -> "C:\\Users\\Me\\configs.ini",
        "--outputPath" -> "C:\\Users\\Me\\out",
        "--help" -> ""
      )
      val args = argMap.map {
        tuple => {
          if ( tuple._2 != "" ) Array( tuple._1, tuple._2 )
          else Array( tuple._1 )
        }
      }.flatten
      val argSet = paramSet.parseArgs( args.toArray )
      assert( argSet( "--configPath" ).valid( 0 ) == argMap( "--configPath" ) )
      assert( argSet( "--outputPath" ).valid( 0 ) == argMap( "--outputPath" ) )
      assert( argSet( "--help" ).found )
      assert( !argSet( "--help" ).valued )
    }

    it( "Any args outside the param net are available as extras." ) {
    
      val extras = Array( "I", "don't", "know", "what", "I'm", "doing..." )
      val args = extras ++ Array( "--help" )
      val argSet = paramSet.parseArgs( args )
      assert( argSet.extraArgs == extras.toVector )
    }

    it( "You can use aliases as readily as names." ) {
    
      val argMap = Map(
        "-c" -> "C:\\Users\\Me\\configs.ini",
        "-o" -> "C:\\Users\\Me\\out",
        "-h" -> ""
      )
      val args = argMap.map {
        tuple => {
          if ( tuple._2 != "" ) Array( tuple._1, tuple._2 )
          else Array( tuple._1 )
        }
      }.flatten
      val argSet = paramSet.parseArgs( args.toArray )
      assert( argSet( "--conf" ).valid( 0 ) == argMap( "-c" ) )
      assert( argSet( "--out" ).valid( 0 ) == argMap( "-o" ) )
      assert( argSet( "/?" ).found )
      assert( !argSet( "/?" ).valued )
    }

    it( "When no options or validators are defined, any args are valid." ) {
      
      val params = ParamSet.define( Vector( Setting.define( "--anything" ) ) )
      val args = params.parseArgs( Array( "--anything", "value" ) )
      assert( args( "--anything" ).found )
      assert( args( "--anything" ).valued )
      assert( args( "--anything" ).invalid.isEmpty )
      assert( args( "--anything" ).valid == Vector( "value" ) )
    }
  }
}


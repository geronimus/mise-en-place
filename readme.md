# Mise-en-place

A little mechanism to help process command-line arguments.

## Components:

### geronimus.cli.Setting

Describes an expected parameter, including the name by which it will be recognized, any aliases, potential values, and validation rules.

```scala
import geronimus.cli.Setting
import java.nio.file.{ Paths, Files }

val config = Setting(
  name = "--configPath",
  required = true,
  description = "The full or relative path to the configuration file.",
  errorText = "The provided configPath was found not to exist.",
  aliases = Set( "--conf", "-c" ),
  validators = Set( { arg => Files.exists( Paths.get( arg ) ) } )
)

val env = Setting(
  name = "--mode",
  required = true,
  default = "dev"
  description = "The mode in which to start up the app.",
  errorText = "The value must be either \"dev\" or \"prod\".",
  aliases = Set( "--env", "-m" ),
  options = Set( "dev", "prod" )
)

val help = Setting(
  name = "--help",
  description = "Pass this flag to display the full help file for this app."
)
```

#### Setting Properties

- __name__ _String_ Required. The parameter's official name. Keep it between 1 and 255 characters.
- __required__ _Boolean_ Optional. The default is `false`. Determines whether or not a value for this parameter must be supplied in an argument.
- __default__ _String_ Optional. The default value that this parameter should take, in the absence of an argument.
- __description__ _String_ Optional. The text you might want to show up explaining the parameter, for example, in help or usage messages. (None of the objects in this library do this for you, but it might help to keep the description together with the parameter's definition.)
- __errorText__ _String_ Optional. The text you might want to show up if a valid argument value is not found. (None of the objects in this library do this for you, but again, it might be convenient to keep error text alongside the parameter's definition.)
- __aliases__ _Set[ String ]_ Optional. A set of alternate names for this parameter. Make sure these don't duplicate its name, or the names and aliases of other parameters in the same parameter set.
- __options__ _Set[ String ]_ Optional. A set of possible argument values. If any options are defined, and an argument does not match one of them, then it is deemed invalid.
- __validators__ _Set[ String => Boolean ]_ Optional. A set of validation functions - taking a String and returning a Boolean - which determine whether an argument is deemed valid. If any are defined, an argument must return `true` for all of them, in order to be deemed valid.

### geronimus.cli.ParamSet

With our settings defined, we can now define the parameter set.

```scala
import geronimus.cli.ParamSet

val params = ParamSet( config, env, help )
```

Assuming that we're inside an object that extends `App`, we can use it to parse the args array.

```scala
val argSet = params.parseArgs( args )
```

### geronimus.cli.ArgSet

`ParamSet.parseArgs( args : Array[ String ] )` returns a `geronimus.cli.ArgSet`, with methods to access and interrogate the passed-in arguments.

```scala
if ( argSet( "--help" ).found )
  displayHelp()
else if ( !argSet( "--configPath" ).valid.isEmpty )
  readConfig( argSet( "--configPath" ).head )
```

An `ArgSet` has many values and methods for querying its parameters and arguments, including:

- __apply( key : String )__ _ArgResult_ Works like ___resultsLookup___, but it will return an empty `ArgResult` even if the `Setting` whose name or alias matches the ___key___ is not defined for this `ArgSet`.
- __isEmpty__ _Boolean_ Returns `true` if no arguments matched a defined parameter setting.
- __hasRequired__ _Boolean_ Returns `true` only if all parameters defined as required are satisfied with valid arguments.
- __missingRequiredParams__ _Set[ Setting ]_ The set containing all parameter Setting objects defined as required, but where no valid value was found.
- __paramsLookup__ _Map[ String, Setting ]_ You can use it to find a parameter `Setting` defined in this `ArgSet`, using its name, or one of its aliases.
- __result( key : String )__ _ArgResult_ Synonym for ___apply___.
- __resultsLookup__ _Map[ String, ArgResult ]_ You can use it to find an `ArgResult` defined in this `ArgSet`, using uts name, or one of its aliases.

### geronimus.cli.ArgResult

The object that an `ArgSet` returns, when you give it the name of a defined (or not defined) parameter (Setting). It is a case class with four properties:

- __found__ _Boolean_ Indicates whether or not an argument with the parameter's name, or alias, was found at all. Most useful if you're just looking for a flag.
- __valued__ _Boolean_ Indicates whether or not any values were found following the parameter's name, or alias. These are presumed to be its intended values. (If any values are found, this property is true, regardless of whether or not they are judged valid.)
- __valid__ _Seq[ String ]_ The arguments found following the parameter's name, or alias, that pass all of the defined validation functions, or that are one of the Setting's allowed options. (You can use both options and validation functions, if you like. If you don't define either, any values are presumed to be valid.) This is a Sequence, rather than a simple value, because it is equally possible to find no values at all, or to find multiple values.
- __invalid__ _Seq[ String ]_ The arguments found following the parameter's name, or alias, that have failed one of the validation functions, or that aren't one of the defined options.

You can find other values and methods for interacting with `Setting`s, `ParamSet`s, and `ArgSet`s, in the project's generated documentation. (Just run: `sbt doc`)


# Traversing lines in Input or ReadChars.
Both Input and ReadChars objects have a method traversing the object one line at a time.
The terminator can be autodetected (which assumes the ending is one of `\n`, `\r\n`, `\r`) or it
can be explicitly declared, or it can be a custom separator such as `;`.

Note: The `lines()` method is lazily evaluated so calling the method without processing will
not result in any processing of the resource.

###### Defaults
Default behaviour with the parameter defaults.  Autodetect ending assuming one of
`\n`, `\r\n` or `\r`.  The terminator is not included in results

```tut:silent
import scalax.io._
import JavaConverters._
val text = "gaf\ngif\ngom\nfoo**bar**baz**".asReadChars
```

```tut
val lines = text.lines()
lines.toList
``` 

###### Auto-detect terminator
Explicitly declare line ending as AutoDetect and include terminator
```tut
import Line.Terminators.Auto

text.lines(Auto,true).toList
``` 

###### Newline terminator
Explicitly declare line ending as NewLine and _do not_ include terminator
```tut
import Line.Terminators.NewLine

text.lines(NewLine,false).toList
``` 

###### Custom terminator
Explicitly declare a custom line terminator (`**`) and _do not_ include terminator
```tut
import Line.Terminators.Custom

text.lines(terminator=Custom("**"), includeTerminator=false).toList
```
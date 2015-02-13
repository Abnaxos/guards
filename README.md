Guards
======

This project provides guards for method parameters and return values using Java annotations. For one side, it provides plugins to check for the correctness of guard declarations in the code, on the other side it provides a Java agent that will instrument the code at runtime so AssertionErrors are thrown if those contracts are violated.

The real power of those contracts will come with static code analysis. There will also be a javac plugin which just checks for correctness and an IDEA plugin, which will integrate it with the currently present static analysis for `@Nullable` and `@NotNull` -- possibly providing more such analysis in the future.

It will also provide compatibility features with IDEA's own `@Nullable`/`@NotNull`/`@Contract` annotations and, of course, JSR305.

You may also add your own guards. Your code will be guarded with them and the javac and IDEA plugins will pick them up and assist you during coding as much as possible.

Note that there are many old JavaDocs that are out-of-date. Some fundamental concepts have been changed. Specifically, the original concept was to implicitly inherit guards. This has been dropped for the following reasons:

 *  Analysis are too complex for class-loading time

 *  The documentative character of the guards is only intact if the annotations are visible where they're in effect. Implicitly inherited guards are much less visible.

 *  Managing guard inheritance has therefore been moved to the IDE, very much as IDEA nowadays handles `@NotNull`/`@Nullable` -- just with many more guards. The IDEA and javac plugins will be essential component of the project as a whole (instead of some bells and whistles, as they were with the original concept).

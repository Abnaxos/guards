ch.raffael.guards
=================

Guards is a code-instrumenting Java agent that instruments classes to check annotated parameters or methods for certain conditions. Think of it as sort of "contracts light": It checks parameter and method return values for validity (not null, not negative, etc.), but it can't correlate those values or check the class' state.

The agent can operate in two modes:

 *  In *assert* mode (the default), the agent will generate `assert` statements for the parameter and return value checks.

 *  In *exception* mode, the agent will generate code to throw `IllegalArgumentException`s for method arguments and `IllegalStateException`s for method return values.

See package [`ch.raffael.guards`](http://projects.raffael.ch/guards/api/index.html?ch/raffael/guards/package-summary.html) for examples.

For information on how to implement your own guards, see package [`ch.raffael.guards.definition`](http://projects.raffael.ch/guards/api/index.html?ch/raffael/guards/definition/package-summary.html).


Using the agent
---------------

Start java with the argument `-javaagent:/path/to/guards-agent.jar[=option1,option2,...]`. The following options are available:

 * **mode=MODE:** Set the mode of the agent. Valid values are *"assert"* (the default) and *"exception"*.

 * **dumpPath=PATH:** Set a path to dump the instrumented bytecode to. By default, the agent doesn't write any dumps.

 * **dumpFormat=FORMAT:** Set the format of the dumps of the instrumented bytecode. Valid values are *"bytecode"* (the default) and *"asm"*.

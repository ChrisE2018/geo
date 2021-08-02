# geo Initial Release

geo simplifies geometry by giving you a graphical way to find mathematical formulas. In the main window you draw lines to form triangles and other shapes marking some variables as known and the program figures out what other variables can be derived. Everything is color coded. Variables that can't be computed are red and variables that can be derived are green. Click anything in green and show the derivation to see the mathematical steps required to compute that value.

The program knows how to compute line length, angle and endpoints. Where two lines cross it creates a vertex with angles. Three connected vertex points form a triangle. Once geo is told a few known triangle variables, it can figure out the rest and give you the equations.

This is useful for programming graphics in situations where you want to display something in relation to another object. Maybe you have two points and want to show a flag a certain distance and angle away. You draw a rough diagram in geo, set the known values and ask for the formula you need. Copy that into your program and away you go.

It can also be useful for engineering or aerospace applications. Support you have a camera at one position observing an aircraft at a known angle and distance. How do you point another camera 100 km away to see the same thing? Ask geo!

This is a preliminary initial release to see if anyone is interested in this concept. There are a few spots that are not rough edges and I don't promise a whole lot more. It is a maven build and there is extensive unit testing (junit 5/jupiter). The geometry engine is separable from the gui, so it could be embedded in another application. The symbolic math uses symja for simplification. That could be replaced with matlab or something else. It would be much more useful if circles could be analyzed but that would require a few more weeks to implement. 

Let me know if you like the idea or find a way to use it. If you want to contribute get in touch. There is a lot that can be added. If you want me to add more features, let me know. If you can provide funding, I'd be happy to implement it...

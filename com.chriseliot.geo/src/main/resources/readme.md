# Geo Introduction

Geo is a graphical geometry analysis tool intended to support graphic programming and engineering purposes.

The Geo tool allows you to sketch geometry diagrams. 
Once you make the lines and triangles the tool will create mathematical formulas to calculate anything you need from what you know.
First you click on variables in the diagram to mark them as known terms.
Then, you click on variables you need and ask for the formula.
If enough terms are known to derive the value you need, the Geo tool will automatically
figure out the math and give you the simplified equation. 
These equations will work for any diagram similar to the one you draw by replacing the variables with any values you have.

Inside the tool are many variations of the law of sines, the law of cosines and other theorems of geometry.
Whenever you click on a variable to make it known, the tool applies all these theorems to derive the other variables.
Anything shown in green has been figured out. 

The tool will give you the math to compute the values you need.

# Future work

This initial release has a lot of rough edges. 
It is meant to get the idea out there and see how much interest there might be in the idea.

## Gui improvements

The gui displays all the variables in the diagram. A simplified view should eliminate some that are duplicates or obviously derived from others.
The names shown in the formulas are created by joining together many words that make sense internally. Better variable names should be
created and shown to make it easier to understand.

The displayed formula should be formatted more clearly. An interface to a LaTex engine would be great.

The outline view needs to expand and contract properly. It should track the selected item in the main window.

The drag line mode only shows a line indicating where the drag point will move to. This should become a live display showing how the whole diagram
will change as you move the mouse.

## Application Improvements

You should be able to save, restore and print a diagram.

## Geometry Improvements

It would be nice to support circles!




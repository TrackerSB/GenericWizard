# Generic Wizard
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/bayern.steinbrecher/Wizard?server=https%3A%2F%2Foss.sonatype.org&style=for-the-badge)

This wizard is a JavaFX library which allows to define a wizard in an abstract and generic manner.
A wizard is organized into pages which can be connected.
For each page you specify the following set of properties:
- A content element
- A function which computes whether the content is valid
- A function which computes which page to show next
- A function which computes the resulting data object of the page

## Features which exceed typical wizards
- Abstraction from the actual content of the wizard pages
- Static type safety through generic parameters which specify the type of the result for each page
- Branching in the sequence of wizard pages
- Dynamic creation of additional wizard pages

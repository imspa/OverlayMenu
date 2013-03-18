OverlayMenu
===========

A library that implements the typical i'm Watch Overlay Menu.

## Installation

Just clone the repository on your computer, then reference the `OverlayMenu` Android Library project from your project. The repository contains both [Eclipse][1] and [IntelliJ IDEA][2] project files for the library project.

## Usage

In order to implement the Overlay Menu in your project, you have to follow these steps:

  1. Use a theme whose parent is `Theme.ImWatch` for the app, or the `Activities` where the `OverlayMenu` is used.
  2. Create a wrapper layout containing both the `OverlayMenu` and the actual layout (you can check out the sample project for an example of this, in `/res/layout/main_wrapper.xml`).

   [1]: http://www.eclipse.org/
   [2]: http://www.jetbrains.com/idea/

## License
This source code is provided under the BSD 3-clause license:

<pre>
Copyright (c) 2013, i'm Spa
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
* Neither the name of the i'm Spa nor the
  names of its contributors may be used to endorse or promote products
  derived from this software without specific prior written permission.
  
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL i'm Spa BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
</pre>

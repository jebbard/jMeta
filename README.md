# jMeta

jMeta is an extensible Java library for reading and writing multimedia metadata and container formats. Currently, jMeta is in an alpha status and supports the following data formats:
* MP3
* OGG
* ID3v1 and ID3v1.1
* ID2v2.3
* APEv2
* Lyrics3v2

MPEG-4/JPEG 2000, RIFF, QuickTime and Matroska as well as ID3v2.4, VorbisComment, Lyrics3v1 and APEv1 support is in planning stages. 

All details and further links for multimedia container data formats and metadata formats can be found in the [following book authored by the creator of this library](https://drive.google.com/file/d/18KWGkUWWp9CIcfzMpfAc4rqdLS2SXWn9/view?usp=sharing).

jMeta lets you:
* Generically read both payload data and parsing metadata of the above mentioned formats down to the bit-level, if you like
* Do so from the following data sources: Input streams, files, byte arrays
* Read from data sources that contain multiple different supported data formats on top-level - the easiest example is an MP3 file consisting of various metadata tags and MP3 frames
* Define your own extensions for other binary container-like data formats to support these formats

Note that writing these data formats is currently in alpha status while also a high-level API to make reading and writing yet more comfortable.

## Architecture

The design and architecture of jMeta is extensively covered in the [jMeta design concept](jMetaDocs/DesignConcept/jMetaDesignConcept.pdf). Here we just give a basic overview of the general architecture of jMeta.

### Container Data Format Metamodel

tbd.

### jMeta Components

tbd.

### Extension Architecture

tbd.

## Developer Documentation

### Projects

tbd.

### jMeta Dependencies

The library is currently using OpenJDK 14. We tried to ensure that jMeta uses as few 3rd-level dependencies as possible and therefor has both a small memory and startup footprint as well as no compatibility and other common dependency issues.

| **Library/Framework/API/Tool** | **Version** | **Category**   | **Purpose** | **Link** |
| ---                            | ---         | ---            | ---         | ---      |
| ByteBuddy                      | LATEST      | Runtime        | Only necessary to ensure mockito works with OpenJDK14            |  |
| jUnit                          | 4.12      | Testing        | Default test runner and assertions | [https://junit.org/junit4/](https://junit.org/junit4/) |
| log4j 2                       | 2.10.0      | Runtime        | Logging Implementation | [https://logging.apache.org/log4j/2.x/](https://logging.apache.org/log4j/2.x/) |
| Mockito                        | 3.3.3    | Testing        | Mocking dependencies for unit testing | [https://site.mockito.org/](https://site.mockito.org/) |
| OpenJDK                        | 14          | Runtime        | JVM | [https://openjdk.java.net/projects/jdk/14/](https://openjdk.java.net/projects/jdk/14/) |
| slf4j                       | 1.7.21      | Runtime        | Logging API | [http://www.slf4j.org/manual.html](http://www.slf4j.org/manual.html) |

All of these versions can be found in the [POM.xml](jMeta/pom.xml).

### Build

tbd.

## Project Planning

This project is currently a one man show, and thus project planning is simply done [within an org mode file](jMetaDocs/jMeta.org).

The following main topics are on the road map as high-level next steps for the project (in no particular order):
* Finish writing functionality for generic interface
* Support for MPEG-4/JPEG 2000, QuickTime, RIFF, Matroska, ID3v2.4, VorbisComment, Lyrics3v1 and APEv1
* Concept for High-Level API (Interface/Annotation-based)
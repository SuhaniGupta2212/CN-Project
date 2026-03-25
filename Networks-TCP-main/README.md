//---------------------------------------------
//              LINE ENCODING TOOL
//---------------------------------------------
This program demonstrates different line encoding and decoding techniques.
It supports both analog and digital input signals and gives graphical representations of the encoded as
waveforms using a Swing-based GUI.

Imported Packages:
- import java.awt.image.BufferedImage;
  Used to store and process analog images during PCM or DM conversion.

- import java.io.File;
  Handles reading of analog image files from disk.

- import java.io.IOException;
  Handles input-output exceptions while reading or writing image files.

- import javax.imageio.ImageIO;
  Reads and writes image files (e.g., PNG, JPG) used in analog signal processing.

- import decoders.*;
  Includes all decoding classes for reversing the line encoding process.

- import encoders.*;
    Includes all encoding classes for encoding the signals.

-------------------------------------------------------
LINE ENCODING
-------------------------------------------------------
Line Encoding is a technique to convert digital bits into
a specific pattern of signal for transmission over a medium.
It defines how 0s and 1s are represented using voltage levels.
It is a language that bits speak while they travel across a medium or cable.

Used to correct:
- DC Component
- Baseline Wandering
- Synchronization issues

-------------------------------------------------------
Supported Encoding Schemes:
-------------------------------------------------------

1. NRZ-L (Non-Return to Zero – Level)
   Logic:
      Bit 1 → High Voltage
      Bit 0 → Low Voltage

2. NRZ-I (Non-Return to Zero – Inverted)
   Logic:
      Bit 1 → Transition (invert the level)
      Bit 0 → No Change

3. Manchester Encoding
   Logic:
      Bit 1 → High → Low
      Bit 0 → Low → High
   It is a mixture of RZ (return to zero) and NRZ-L (non-return to zero–level),
   because of which it has a mid-bit transition.

4. Differential Manchester Encoding
   Logic:
      Always a mid-bit transition
      Bit 0 → Transition at start
      Bit 1 → No transition at start

5. AMI (Alternate Mark Inversion)
   Logic:
      Bit 0 → No pulse (0V)
      Bit 1 → Alternates between +V and -V

-------------------------------------------------------
SCRAMBLING TECHNIQUES
-------------------------------------------------------
Used with AMI for long data streams to prevent loss of synchronization.

- B8ZS
  Logic:
    When 8 consecutive 0s occur, they are replaced with a special pattern that includes
    two bipolar violations.
    Pattern: 000VB0VB

- HDB3
  Logic:
    Replaces every four 0s with 000V or B00V depending on polarity and number
    of preceding nonzero pulses.
    Odd count  → 000V
    Even count → B00V
    (V = violation, B = balancing pulse)

-------------------------------------------------------
Common Java Utilities Used
-------------------------------------------------------
| Function / Class        | Purpose / Description                                                   |
|--------------------------|------------------------------------------------------------------------|
| StringBuilder            | Builds and modifies strings efficiently (used in encoding logic).      |
| .append()                | Adds text to StringBuilder content.                                   |
| .reverse()               | Reverses text (used in palindrome detection).                         |
| .toString()              | Converts StringBuilder to a normal String.                            |
| .trim()                  | Removes spaces from user input.                                       |
| ArrayList / .add()       | Stores encoded signal elements (voltage levels).                      |
| switch-case              | Chooses encoding/decoding method (NRZ-L, Manchester, etc.).           |
| for / for-each loops     | Iterates through bits for encoding and decoding.                      |
| if-else conditions       | Implements logical decisions (e.g., polarity inversion).               |
| String                   | Represents bitstream (sequence of 0s and 1s).                         |
| .charAt()                | Reads individual bits from input.                                     |
| .equals()                | Compares string equality (palindrome check, etc.).                    |

-------------------------------------------------------
Summary:
- The program lets the user select analog or digital input.
- Encodes data using chosen line encoding.
- Optionally decodes back to verify correctness.
- Displays waveform graphically with voltage transitions.

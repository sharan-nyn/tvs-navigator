
<img src="/readme/logo.png" align="left"
width="150" hspace="10" vspace="10">

# TVS Navigator
App to navigate Apache RTR using Google Maps notifications.
<br/>
## About

The official application offered by TVS has navigation features. Unfortunately the map being used is MapMyIndia. There are limitations of using MapMyIndia like not having enough locations tagged and choosing route depending on live traffic.
The app works by parsing the Google Maps notifications and creating the commands to be sent to the bike's console.
I used bt-snoop logs and unpacked the official app to reverse engineer the instruction set.
This is certainly not a replacement for official app offered by the manufacturer, there are many features missing which I may add in the future.
I like to create solutions to the problems I face in real life using code, the quality of the code may not be perfect but hey as long as it works :)

## Features

The app lets you:
- Send the Google Maps navigation directions to the bike's console
- Displays the remaining distance and ETA time information
- Displays information about people calling, also sends SMS to a contact with information like ETA and current location.

## How It Works
The Google Maps notifications are read by the GMapsParser. 
GMaps notification contains the direction as a bitmap, the direction is identified by comparing it with a repo of possible bitmaps (app\src\main\assets).

#### TVS Apache RTR console instruction set
The instructions are encoded in hexadecimal.
Some sample instructions:
| Instruction   | Hexadecimal   |
| ------------- |:-------------:|
| 50M TURN RIGHT |4e01f40003015455524e20524947485424|
| REROUTING      | 4e00000063015265726f7574696e6724 |

Let's decode them:
| N/O | Distance      | Unit | Pictogram/MessageId | Multiline Flag | Text Message         | $  |
| --- | ------------- | ---- | ------------------- | -------------- | -------------------- | -- |
| 4e  | 01 f4 (050.0) | 00   | 03 (03)             | 01             | 5455524e205249474854 (TURN RIGHT) | 24 |
| 4e  | 00 00 (000.0) | 00   | 63 (99)             | 01             | 5265726f7574696e67 (REROUTING)   | 24 |

| Unit Values | Symbol |
| ----------- | ------ |
| 00          | M      |
| 01          | KM     |

- Multiline flags are used for big instructions are which sent in two parts separated by 100ms.
- The pictogram id is a unique id for the images shown in the console.

## Screenshots
[<img src="/readme/screenshots/Screenshot_20210513-173948457.jpg" align="left"
width="200"
    hspace="10" vspace="10">](/readme/screenshots/Screenshot_20210513-173948457.jpg)

[<img src="/readme/screenshots/Screenshot_20210513-173945084.jpg" align="center"
width="200"
    hspace="10" vspace="10">](/readme/screenshots/Screenshot_20210513-173945084.jpg)

## Permissions

Requires the following permissions:
- Call logs - For displaying incoming call details.
- Contacts - For showing who's calling.
- Location - Used for BLE.
- Phone - For displaying incoming call details.
- SMS - For sending SMS to the contact in case of missed call.
- Notification Access - To read GMaps notifications.

## Libraries Used
- [GMapsParser](https://github.com/3v1n0/GMapsParser) - used for reading Google Maps notification data
- [blessed-android](https://github.com/weliem/blessed-android) - used for BLE communications
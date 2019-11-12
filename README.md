[![Build Status](https://api.travis-ci.org/envirover/KVAdapter.svg?branch=master)](https://travis-ci.org/envirover/KVAdapter)

# Kinesis Video Streams Adapter

Kinesis Video Streams Adapter is a TCP server application that serves
video from a Amazon Kinesis Video stream to connected clients such as
GStreamer multimedia framework.

The adaptor was originally made to support low latency video streaming from
Amazon Kinesis Video stream to [QGroundControl](https://github.com/mavlink/qgroundcontrol/blob/master/src/VideoStreaming/README.md) ground control station. However, it
is fairly generic and can be used for other applications.

[Amazon Kinesis Video Streams parser library](https://github.com/aws/amazon-kinesis-video-streams-parser-library)
has an example for continuously piping the output of GetMedia calls from a
Kinesis Video stream to GStreamer. However, the example uses fdsrc GStreamer 
element that is not supported on Windows.

## Installation

1. [Create a Kinesis Video stream](https://docs.aws.amazon.com/kinesisvideostreams/latest/dg/getting-started.html)
2. Create [IAM user](https://docs.aws.amazon.com/kinesisvideostreams/latest/dg/how-iam.html) that has permissions to get media
from the stream and configure the user's AWS access key Id and secret access key in %UserProfile%\.aws\credentials file.

    ```text
    [default]
    aws_access_key_id=AKIAIOSFODNN7EXAMPLE
    aws_secret_access_key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
    ```

3. Install JRE 8
4. Unzip [kvadapter-bin.zip](https://github.com/envirover/KVAdapter/releases) to a local folder and set environment variable KVADAPTER_HOME to that folder path.
5. Install GStreamer.
   > For use with QGroundControl it's recommended to install GStreamer 1.4.14 from [https://gstreamer.freedesktop.org/data/pkg/windows/](https://gstreamer.freedesktop.org/data/pkg/windows/).

## Use

To just start Kinesis Video Streams adapter run PowerShell script kvadapter.ps1:

```PowerShell
powershell $Env:KVADAPTER_HOME\bin\kvadapter.ps1 -r <AWS region> -s <stream name>
```

To start Kinesis Video Streams adapter and a GStreamer pipeline that streames 
the h.264 encoded video from the adapter to UDP port 5600 run PowerShell script 
kv2udp.ps1:

```PowerShell
powershell $Env:KVADAPTER_HOME\bin\kv2udp.ps1 -r <AWS region> -s <stream name>
```

To test the UPD video stream open file etc\kvstream.sdp in [VLC](https://www.videolan.org/vlc/index.html) media player.

## Build

To build Kinesis Video Streams Adapter:

1. Install JDK 8.
2. Install Maven
3. Install Git
4. Clone the source code:

   ```shell
   git clone  git@github.com:envirover/KVAdapter.git
   ```
5. Run Maven

   ```shell
   mvn clean install
   ```

## Issues

Find a bug or want to request a new feature?  Please let us know by submitting an [issue](https://github.com/envirover/KVAdapter/issues).

## Contributing

Envirover welcomes contributions from anyone and everyone. Please see our [guidelines for contributing](https://github.com/envirover/KVAdapter/blob/master/CONTRIBUTING.md).

## License

Copyright 2019 Envirover. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License").
You may not use this file except in compliance with
the License. A copy of the License is located at

https://github.com/envirover/KVAdapter/blob/master/LICENSE

or in the "license" file accompanying this file. This file is distributed on
an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
express or implied. See the License for the specific language governing
permissions and limitations under the License.

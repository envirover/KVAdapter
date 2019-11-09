# Starts Kinesis Video adapter that listens on TCP port 4000.
#
# Usage: kvadapter.ps1 -r <AWS region> -s <Kinesis Video stream name> -p <port>

if ($Env:KVADAPTER_HOME -eq "") {
    Write-Output "Environment variable KVADAPTER_HOME is not set."
    exit 1
}

$classpath = $Env:KVADAPTER_HOME + "\lib\*"

Start-Process java -ArgumentList "-cp $classpath com.envirover.video.KinesisVideoAdapter $args" -PassThru -NoNewWindow -Wait

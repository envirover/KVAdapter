# Starts Kinesis Video adapter and GStreamer pipeline that receives 
# video matroska h264 encoded video from TCP port 4000 and sends it
# to UDP port 5600.
#
# Usage: kv2udp.ps1 -r <AWS region> -s <Kinesis Video stream> 

if ($Env:KVADAPTER_HOME -eq "") {
    Write-Output "Environment variable KVADAPTER_HOME is not set."
    exit 1
}

$pipeline = "-v tcpclientsrc port=4000 ! matroskademux ! avdec_h264 ! videoconvert ! x264enc ! rtph264pay ! udpsink host=127.0.0.1 port=5600"

$classpath = $Env:KVADAPTER_HOME + "\lib\*"

$kvadapter = Start-Process java -ArgumentList "-cp $classpath com.envirover.video.KinesisVideoAdapter $args" -PassThru -NoNewWindow

$gstreamer = Start-Process gst-launch-1.0 -ArgumentList $pipeline -PassThru -NoNewWindow

Wait-Process -Id $gstreamer.Id

Stop-Process -Id $kvadapter.Id
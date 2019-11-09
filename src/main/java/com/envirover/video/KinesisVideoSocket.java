package com.envirover.video;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Optional;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.kinesisvideo.parser.ebml.InputStreamParserByteSource;
import com.amazonaws.kinesisvideo.parser.mkv.FrameProcessException;
import com.amazonaws.kinesisvideo.parser.mkv.MkvElementVisitException;
import com.amazonaws.kinesisvideo.parser.mkv.MkvElementVisitor;
import com.amazonaws.kinesisvideo.parser.mkv.StreamingMkvReader;
import com.amazonaws.kinesisvideo.parser.utilities.FragmentMetadata;
import com.amazonaws.kinesisvideo.parser.utilities.OutputSegmentMerger;
import com.amazonaws.kinesisvideo.parser.utilities.consumer.FragmentMetadataCallback;
import com.amazonaws.kinesisvideo.parser.utilities.consumer.FragmentProgressTracker;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideo;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideoClientBuilder;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideoMedia;
import com.amazonaws.services.kinesisvideo.AmazonKinesisVideoMediaClientBuilder;
import com.amazonaws.services.kinesisvideo.model.APIName;
import com.amazonaws.services.kinesisvideo.model.GetDataEndpointRequest;
import com.amazonaws.services.kinesisvideo.model.GetMediaRequest;
import com.amazonaws.services.kinesisvideo.model.GetMediaResult;
import com.amazonaws.services.kinesisvideo.model.StartSelector;
import com.amazonaws.services.kinesisvideo.model.StartSelectorType;

import org.apache.commons.lang3.Validate;

/**
 * Streams Kinesis Video stream to client socket.
 */
class KinesisVideoSocket {

    private static final int HTTP_STATUS_OK = 200;

    private final String region;
    private final String streamName;
    private final Socket socket;

    /**
     * Constructs KinesisVideoSocket instance for the specified stream and socket.
     *
     * @param region
     * @param streamName
     * @param socket
     */
    public KinesisVideoSocket(final String streamName, final String region, final Socket socket) {
        this.region = region;
        this.streamName = streamName;
        this.socket = socket;
    }

    /**
     * Starts video streaming until the socket is closed.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void startStreaming() throws IOException, InterruptedException {
        try (OutputStream os = socket.getOutputStream()) {
            AmazonKinesisVideoClientBuilder builder = AmazonKinesisVideoClientBuilder.standard().withRegion(region);
            AmazonKinesisVideo amazonKinesisVideo = builder.build();
            String endPoint = amazonKinesisVideo
                    .getDataEndpoint(
                            new GetDataEndpointRequest().withAPIName(APIName.GET_MEDIA).withStreamName(streamName))
                    .getDataEndpoint();

            AmazonKinesisVideoMediaClientBuilder mediaClientBuilder = AmazonKinesisVideoMediaClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, region))
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance());
            AmazonKinesisVideoMedia videoMedia = mediaClientBuilder.build();

            StartSelector startSelector = new StartSelector().withStartSelectorType(StartSelectorType.NOW);
            Optional<String> fragmentNumberToStartAfter = Optional.empty();
            MediaCallback callback = new MediaCallback();

            while (true) {
                GetMediaResult getMediaResult = null;
                try {
                    StartSelector selectorToUse = fragmentNumberToStartAfter
                            .map(fn -> new StartSelector().withStartSelectorType(StartSelectorType.NOW))
                            .orElse(startSelector);
                    getMediaResult = videoMedia.getMedia(
                            new GetMediaRequest().withStreamName(streamName).withStartSelector(selectorToUse));
                    System.err.printf("Start processing GetMedia called for stream '%s'.\n", streamName);
                    if (getMediaResult.getSdkHttpMetadata().getHttpStatusCode() == HTTP_STATUS_OK) {
                        OutputSegmentMerger merger = OutputSegmentMerger.createToStopAtFirstNonMatchingSegment(os);
                        processWithFragmentEndCallbacks(getMediaResult.getPayload(), callback, merger);
                    } else {
                        Thread.sleep(200);
                    }
                } catch (FrameProcessException e) {
                    System.err.println("FrameProcessException in ContinuousGetMedia worker for stream: " + streamName);
                    System.err.print(e);
                    //break;
                } catch (MkvElementVisitException e) {
                    System.err.println("Failure in ContinuousGetMedia worker for stream: " + streamName);
                    System.err.println(e.getMessage());
                    if (e.getCause() != null && e.getCause().getClass() == java.net.SocketException.class) {
                        System.err.println(e.getCause().getMessage());
                        throw (java.net.SocketException) e.getCause();
                    }
                }
            }
        }
    }

    protected void processWithFragmentEndCallbacks(InputStream inputStream,
            FragmentMetadataCallback endOfFragmentCallback, MkvElementVisitor mkvElementVisitor)
            throws MkvElementVisitException {
        StreamingMkvReader.createDefault(new InputStreamParserByteSource(inputStream))
                .apply(FragmentProgressTracker.create(mkvElementVisitor, endOfFragmentCallback));
    }

    static class MediaCallback implements FragmentMetadataCallback {
        private Optional<String> fragmentNumberToStartAfter = Optional.empty();

        public void call(FragmentMetadata f) {
            Validate.isTrue(!fragmentNumberToStartAfter.isPresent()
                    || f.getFragmentNumberString().compareTo(fragmentNumberToStartAfter.get()) > 0);
            fragmentNumberToStartAfter = Optional.of(f.getFragmentNumberString());
        }
    }

}

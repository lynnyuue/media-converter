package lynn.media.converter;

import ws.schild.jave.AudioInfo;
import ws.schild.jave.EncoderProgressListener;
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.VideoInfo;

/**
 * listener to print convert progress
 *
 * @author Lynn
 */
public class ConvertProgressListener implements EncoderProgressListener {

    @Override
    public void sourceInfo(MultimediaInfo multimediaInfo) {
        System.out.println("Process media starting...");
        System.out.println("audio decoder: " + multimediaInfo.getAudio().getDecoder());
        AudioInfo audioInfo = multimediaInfo.getAudio();
        if(audioInfo != null){
            System.out.println("=============");
            System.out.println("audio sampling rate: " + audioInfo.getSamplingRate());
            System.out.println("audio bit rate: " + audioInfo.getBitRate());
        }
        VideoInfo videoInfo = multimediaInfo.getVideo();
        if(videoInfo != null){
            System.out.println("=============");
            System.out.println("video bit rate: " + videoInfo.getBitRate());
            System.out.println("video frame rate: " + videoInfo.getFrameRate());
        }
    }

    @Override
    public void progress(int i) {
        double progress = i / 10.00;
        System.out.println("processing: " + progress + "%");
    }

    @Override
    public void message(String s) {
        System.out.println(s);
    }
}

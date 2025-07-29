import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.protobuf.ByteString;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class VoiceExample {

    public static void main(String[] args) throws Exception {

        String apiKey = "fa70408a89a5341d2175a2450ac5d86eed4e7809"; // Replace with your actual API key
        String textToSynthesize = "Hello, this is a text-to-speech example.";

        try (TextToSpeechClient textToSpeechClient = createTextToSpeechClient(apiKey)) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(textToSynthesize).build();

            // Build the voice request
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US") // Set the language code
                            .build();

            // Select the type of audio file you want
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            // Perform the text-to-speech request
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio content
            ByteString audioContents = response.getAudioContent();

            // Write the audio content to an output file
            try (OutputStream out = new FileOutputStream("output.mp3")) {
                out.write(audioContents.toByteArray());
                System.out.println("Audio content written to file 'output.mp3'");
            }
        } catch (Exception e) {
            System.err.println("Error synthesizing speech: " + e.getMessage());
        }
    }

    private static TextToSpeechClient createTextToSpeechClient(String apiKey) throws IOException {
        TextToSpeechSettings textToSpeechSettings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(ServiceAccountCredentials
                        .fromStream(Files.newInputStream(Paths.get("/Users/susanjaglal/Desktop/CNIB-Research/gaming_resources/Java_Resources/space_invaders/e-centaur-348521-fa70408a89a5.json"))))) //Replace with your service account json file path
                .build();
        return TextToSpeechClient.create(textToSpeechSettings);
    }
}
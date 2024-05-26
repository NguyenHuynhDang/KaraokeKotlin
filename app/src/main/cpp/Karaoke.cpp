//
// Created by HuynhDang on 12/05/2024.
//

#include <unistd.h>
#include <Superpowered.h>
#include <SuperpoweredSimple.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <math.h>
#include "Karaoke.h"

const float MIC_MAX_VOLUME = 0.005f;

bool audioProcessing(void* clientData, short int* audioIO, int numFrames, int sampleRate)
{
    return ((Karaoke*) clientData)->process(audioIO, (unsigned int)numFrames, (unsigned int)sampleRate);
}

Karaoke::Karaoke(unsigned int sampleRate, unsigned int bufferSize)
{
    Superpowered::Initialize("ExampleLicenseKey-WillExpire-OnNextUpdate");

    audioIO = new SuperpoweredAndroidAudioIO (
            sampleRate, // device sample rate
            bufferSize, // device buffer size
            true,   // enable input
            true,   // enable output
            audioProcessing,    // audio callback function
            this,   // client data
            SL_ANDROID_STREAM_VOICE,   // input stream type
            SL_ANDROID_RECORDING_PRESET_VOICE_RECOGNITION
    ); // output stream type

    autotune = new Superpowered::AutomaticVocalPitchCorrection();
    autotune->speed = Superpowered::AutomaticVocalPitchCorrection::EXTREME;
    autotune->clamp = Superpowered::AutomaticVocalPitchCorrection::OFF;

    echo = new Superpowered::Echo((unsigned int)sampleRate);
    echo->setMix(0.0f);
    echo->enabled = isEffectEnable;

    reverb = new Superpowered::Reverb((unsigned int)sampleRate);
    reverb->mix = 0.0f;
    reverb->enabled = isEffectEnable;
}

bool Karaoke::process(short int *audio, unsigned int numFrames, unsigned int sampleRate)
{
    float floatBuffer[numFrames * 2];
    Superpowered::ShortIntToFloat(audio, floatBuffer, numFrames);
    Superpowered::Volume(floatBuffer, floatBuffer, premicVol, micVolume, numFrames);
    premicVol = micVolume;

    if (isAutotuneEnable)
    {
        autotune->samplerate = sampleRate;
        autotune->process(floatBuffer, floatBuffer, true, numFrames);
    }

    if (isEffectEnable) {
        echo->process(floatBuffer, floatBuffer, numFrames);
        reverb->process(floatBuffer, floatBuffer, numFrames);
    }

    Superpowered::FloatToShortInt(floatBuffer, audio, numFrames);
    return true;
}

void Karaoke::setEffectEnable(bool value)
{
    isEffectEnable = value;
    echo->enabled = reverb->enabled = value;
}

void Karaoke::setAutotuneEnable(bool value)
{
    isAutotuneEnable = value;
}

void Karaoke::setEchoValue(int value)
{
    echo->setMix(value / 100.0f);
}

void Karaoke::setReverbValue(int value)
{
    reverb->mix = value / 100.0f;
}

void Karaoke::setMicVolume(float value)
{
    micVolume = value;
}

void Karaoke::stopRecord()
{
    if (audioIO)
    {
        delete audioIO;
        audioIO = nullptr;
    }
    if (echo)
    {
        delete echo;
        echo = nullptr;
    }
    if (reverb)
    {
        delete reverb;
        reverb = nullptr;
    }
    if (autotune)
    {
        delete autotune;
        autotune = nullptr;
    }
}
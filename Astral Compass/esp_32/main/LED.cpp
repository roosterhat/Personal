#include <LED.h>
#include <main.h>
#include <math.h>

int _color[] = {0,0,0};
int _onPeriod = 1e6, _offPeriod = 0, _totalPeriod = 1e6;
int LEDS[] = {LED_R, LED_G, LED_B};

void UpdateLEDs() {
    if(esp_timer_get_time() % _totalPeriod < _onPeriod) {
        for(int i = 0; i < 3; i++)
            ledcWrite(LEDS[i], _color[i]);
    }
    else {
        for(int i = 0; i < 3; i++)
            ledcWrite(LEDS[i], 0);
    }
}

void SetLEDs(int color[3], int onPeriod, int offPeriod) {
    for(int i = 0; i < 3; i++)
        _color[i] = fmin(fmax(color[i], 0), 255);
    _onPeriod = onPeriod * 1000;
    _offPeriod = offPeriod * 1000;
    _totalPeriod = fmax(_onPeriod + _offPeriod, 1);
}

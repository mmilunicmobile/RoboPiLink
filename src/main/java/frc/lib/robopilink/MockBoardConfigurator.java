package frc.lib.robopilink;

import java.util.Arrays;

import com.diozero.api.DeviceMode;
import com.diozero.sbc.BoardPinInfo;

public class MockBoardConfigurator {
    public void configure(BoardPinInfo boardPinInfo) {
        for (int i = 2; i <= 40; i++) {
            boardPinInfo.addGpioPinInfo(i, i, Arrays.asList(DeviceMode.DIGITAL_INPUT, DeviceMode.DIGITAL_OUTPUT));
        }
    }
}

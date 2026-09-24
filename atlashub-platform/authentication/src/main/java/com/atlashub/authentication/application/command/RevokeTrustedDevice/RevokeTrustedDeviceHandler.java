package com.atlashub.authentication.application.command.RevokeTrustedDevice;

import com.atlashub.authentication.domain.entities.TrustedDevice;
import com.atlashub.authentication.domain.repositories.TrustedDeviceRepository;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.AuthorizationException;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class RevokeTrustedDeviceHandler extends Command<RevokeTrustedDeviceCommand, Void> {

    private final TrustedDeviceRepository deviceRepository;

    public RevokeTrustedDeviceHandler(TrustedDeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Void execute(RevokeTrustedDeviceCommand command) {
        TrustedDevice device = deviceRepository.findById(command.deviceId())
                .orElseThrow(() -> new NotFoundException("Device not found"));

        if (!device.getUserId().equals(command.userId())) {
            throw new AuthorizationException("You do not own this device");
        }

        deviceRepository.deleteById(command.deviceId());
        return null;
    }
}

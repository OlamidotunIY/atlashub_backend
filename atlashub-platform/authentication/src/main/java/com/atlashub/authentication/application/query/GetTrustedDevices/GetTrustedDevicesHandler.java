package com.atlashub.authentication.application.query.GetTrustedDevices;

import com.atlashub.authentication.domain.entities.TrustedDevice;
import com.atlashub.authentication.domain.repositories.TrustedDeviceRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetTrustedDevicesHandler extends Query<GetTrustedDevicesQuery, List<TrustedDeviceResult>> {

    private final TrustedDeviceRepository deviceRepository;

    public GetTrustedDevicesHandler(TrustedDeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public List<TrustedDeviceResult> execute(GetTrustedDevicesQuery query) {
        List<TrustedDevice> devices = deviceRepository.findAllByUserId(query.userId());
        return devices.stream()
                .map(d -> new TrustedDeviceResult(
                        d.getId(),
                        d.getDeviceFingerprint(),
                        d.getDeviceName(),
                        d.getLastSeenIp(),
                        d.getTrustedAt(),
                        d.getExpiresAt()
                ))
                .collect(Collectors.toList());
    }
}

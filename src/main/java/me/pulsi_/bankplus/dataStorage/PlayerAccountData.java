package me.pulsi_.bankplus.dataStorage;

import java.math.BigDecimal;
import java.util.UUID;

public record PlayerAccountData(String bankName, UUID uuid, String accountName, int bankLevel, BigDecimal money, BigDecimal interest, BigDecimal debt) {

}

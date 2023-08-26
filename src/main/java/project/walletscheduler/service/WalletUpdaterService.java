package project.walletscheduler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.walletscheduler.domain.Wallet;
import project.walletscheduler.domain.WalletQueue;
import project.walletscheduler.repository.WalletQueueRepository;
import project.walletscheduler.repository.WalletRepository;

@Service
@RequiredArgsConstructor
public class WalletUpdaterService {

    private final WalletRepository walletRepository;
    private final WalletQueueRepository walletQueueRepository;

    @Transactional
    public void updateWallet(WalletQueue walletQueue) {
        Wallet wallet = walletRepository.findByIdWithPessimisticLock(walletQueue.getWallet().getId());

        wallet.changeBalance(walletQueue.getBalances());
        walletQueueRepository.delete(walletQueue);
    }
}
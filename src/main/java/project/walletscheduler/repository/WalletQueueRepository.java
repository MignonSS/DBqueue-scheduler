package project.walletscheduler.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.walletscheduler.domain.WalletQueue;

import java.util.List;

public interface WalletQueueRepository extends JpaRepository<WalletQueue, Long> {

    WalletQueue findFirstByOrderByCreatedAt();

    @Query("select wq from WalletQueue wq join fetch wq.wallet w where wq.id > :lastWalletQueueId")
    List<WalletQueue> getWalletQueues100OfEach(Long lastWalletQueueId, Pageable pageable);

    @Query("select wq from WalletQueue wq join fetch wq.wallet w")
    List<WalletQueue> getWalletQueues100OfEachV2(Pageable pageable);

}

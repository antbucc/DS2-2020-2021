package scuttlebutt.store.protocol;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;
import scuttlebutt.configuration.ScuttlebuttConfiguration;
import scuttlebutt.event.Content;
import scuttlebutt.event.Content.ContentType;
import scuttlebutt.event.Event;
import scuttlebutt.log.Log;
import scuttlebutt.store.Frontier;
import scuttlebutt.store.Store;

/**
 * Transitive Interest specific method implementations
 */
public class TransitiveInterestProtocol implements Protocol {

  /**
   * On heartbeat reception, follow, unfollow, block and unblock with given probabilities
   */
  @Override
  public void processHeartbeat(Store store, PublicKey id) {
    if (store.followProbabilityUniform
        .nextDouble() <= ScuttlebuttConfiguration.FOLLOW_PROBABILITY) {
      follow(store, id);
    }
    if (store.unfollowProbabilityUniform
        .nextDouble() <= ScuttlebuttConfiguration.UNFOLLOW_PROBABILITY) {
      unfollow(store, id);
    }
    if (store.blockProbabilityUniform.nextDouble() <= ScuttlebuttConfiguration.BLOCK_PROBABILITY) {
      block(store, id);
    }
    if (store.unblockProbabilityUniform
        .nextDouble() <= ScuttlebuttConfiguration.UNBLOCK_PROBABILITY) {
      unblock(store, id);
    }
  }


  /**
   * Compute frontier based on stored logs (direct followees) and on logs of followee (transitive
   * followee), taking care also of blocked stores
   */
  @Override
  public Frontier getFrontier(Store store) {
    Set<PublicKey> directFollowee = getFollowed(store.get(store.getPublicKey()));
    Set<PublicKey> blocked = getBlocked(store.get(store.getPublicKey()));
    directFollowee.removeAll(blocked);
    store.setDirectFollowee(directFollowee);
    store.setBlocked(blocked);

    for (PublicKey id : blocked) {
      if (store.getLogs().containsKey(id)) {
        store.remove(id);
      }
    }

    Set<PublicKey> transitiveFollowee = new HashSet<PublicKey>(directFollowee);
    for (PublicKey id : directFollowee) {
      if (store.getLogs().containsKey(id)) {
        Set<PublicKey> indirectFollowee = getFollowed(store.get(id));
        Set<PublicKey> indirectBlocked = getBlocked(store.get(id));
        indirectFollowee.removeAll(indirectBlocked);
        indirectFollowee.removeAll(blocked);
        transitiveFollowee.addAll(indirectFollowee);
      }
    }

    store.setTransitiveFollowee(transitiveFollowee);

    Frontier frontier = new Frontier();
    for (PublicKey id : transitiveFollowee) {
      if (!store.getLogs().containsKey(id)) {
        store.add(new Log(id));
      }
      frontier.put(id, store.get(id).getLastEventStored());
    }

    return frontier;
  }


  /**
   * Compute followee by parsing log and looking for FOLLOW and UNFOLLOW events
   * 
   * @param log: the log to parse
   * @return the set of followee
   */
  private Set<PublicKey> getFollowed(Log log) {
    Set<PublicKey> followed = new HashSet<PublicKey>();
    for (Event event : log.get()) {
      if (event.getContent().getType().equals(ContentType.FOLLOW)) {
        followed.add(event.getContent().getWho());
      }
      if (event.getContent().getType().equals(ContentType.UNFOLLOW)) {
        followed.remove(event.getContent().getWho());
      }
    }
    return followed;
  }


  /**
   * Compute blocked stores by parsing log and looking for BLOCK and UNBLOCK events
   * 
   * @param log: the log to parse
   * @return the set of blocked stores
   */
  private Set<PublicKey> getBlocked(Log log) {
    Set<PublicKey> blocked = new HashSet<PublicKey>();
    for (Event event : log.get()) {
      if (event.getContent().getType().equals(ContentType.BLOCK)) {
        blocked.add(event.getContent().getWho());
      }
      if (event.getContent().getType().equals(ContentType.UNBLOCK)) {
        blocked.remove(event.getContent().getWho());
      }
    }
    return blocked;
  }


  /**
   * Follow a store by creating a FOLLOW event
   * 
   * @param store: the store that follows
   * @param id: the id of the store that is followed
   */
  public void follow(Store store, PublicKey id) {
    Content content = new Content(ContentType.FOLLOW, id);
    store.append(content);
  }


  /**
   * Unfollow a store by creating an UNFOLLOW event
   * 
   * @param store: the store that unfollows
   * @param id: the id of the store that is unfollowed
   */
  public void unfollow(Store store, PublicKey id) {
    Content content = new Content(ContentType.UNFOLLOW, id);
    store.append(content);
  }


  /**
   * Block a store by creating a BLOCK event
   * 
   * @param store: the store that blocks
   * @param id: the id of the store that is blocked
   */
  public void block(Store store, PublicKey id) {
    Content content = new Content(ContentType.BLOCK, id);
    store.append(content);
  }


  /**
   * Unblock a store by creating an UNBLOCK event
   * 
   * @param store: the store that unblocks
   * @param id: the id of the store that is unblocked
   */
  public void unblock(Store store, PublicKey id) {
    Content content = new Content(ContentType.UNBLOCK, id);
    store.append(content);
  }

}

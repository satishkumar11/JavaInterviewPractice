package observerpattern;

// OBSERVER PATTERN — One object (like a YouTube channel) automatically notifies a
// list of subscribers whenever something happens (like uploading a video).

import java.util.ArrayList;
import java.util.List;

// 1. Observer interface — anyone who wants to "subscribe" must implement this
interface Subscriber {
    void notify(String videoTitle);
}

// 2. Concrete observers — different subscribers, each reacts in their own way
class EmailSubscriber implements Subscriber {
    private String name;

    public EmailSubscriber(String name) {
        this.name = name;
    }

    public void notify(String videoTitle) {
        System.out.println("Email to " + name + ": New video uploaded - " + videoTitle);
    }
}

class SmsSubscriber implements Subscriber {
    private String name;

    public SmsSubscriber(String name) {
        this.name = name;
    }

    public void notify(String videoTitle) {
        System.out.println("SMS to " + name + ": New video uploaded - " + videoTitle);
    }
}

// 3. Subject — keeps a list of subscribers and notifies all of them when something happens
class YoutubeChannel {
    private List<Subscriber> subscribers = new ArrayList<>();

    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void uploadVideo(String title) {
        System.out.println("Channel uploaded: " + title);
        for (Subscriber subscriber : subscribers) {
            subscriber.notify(title); // notify everyone automatically
        }
    }
}

// Main class to test it
public class ObserverPatternDemo {
    public static void main(String[] args) {
        YoutubeChannel channel = new YoutubeChannel();

        channel.subscribe(new EmailSubscriber("Satish"));
        channel.subscribe(new SmsSubscriber("Ravi"));

        channel.uploadVideo("Observer Pattern Explained");
    }
}

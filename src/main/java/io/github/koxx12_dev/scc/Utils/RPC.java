package io.github.koxx12_dev.scc.Utils;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.DiscordEventAdapter;
import de.jcm.discordgamesdk.RelationshipManager;
import de.jcm.discordgamesdk.activity.Activity;
import de.jcm.discordgamesdk.user.DiscordUser;
import de.jcm.discordgamesdk.user.Relationship;
import io.github.koxx12_dev.scc.GUI.SCCConfig;
import io.github.koxx12_dev.scc.SCC;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;

public class RPC extends Thread {

    public static RPC INSTANCE = new RPC();

    private Thread trd = new Thread(this);

    private static Instant timestamp = Instant.now();

    public void RPCManager() {

        trd.start();
    }

    public void run() {

        try {
            File discordLibrary = DownloadSDK.downloadDiscordLibrary();
            if(discordLibrary == null) {
                System.err.println("Error downloading Discord SDK.");
                System.exit(-1);
            }
            // Initialize the Core
            Core.init(discordLibrary);

            // Set parameters for the Core
            try(CreateParams params = new CreateParams()) {
                params.setClientID(857240025288802356L);
                params.setFlags(CreateParams.getDefaultFlags());
                // Create the Core

                params.registerEventHandler(new DiscordEventAdapter()
                {

                    @Override
                    public void onActivityJoinRequest(DiscordUser user)
                    {
                        System.out.println("DiscordTest.onActivityJoinRequest");
                        System.out.println("user = " + user);
                    }

                    @Override
                    public void onRelationshipRefresh()
                    {
                        // for debugging
                        System.out.println("RelationshipExample.onRelationshipRefresh");

                        // We are now ready to read information about relationships

                        // filter for all our friends
                        SCC.RPCcore.relationshipManager().filter(RelationshipManager.FRIEND_FILTER);
                        int friendCount = SCC.RPCcore.relationshipManager().count(); // get how many relationships match our filter

                        // filter for all our online friends (previous filter is reset automatically)
                        SCC.RPCcore.relationshipManager().filter(RelationshipManager.FRIEND_FILTER.and(RelationshipManager.ONLINE_FILTER));
                        int onlineFriendCount = SCC.RPCcore.relationshipManager().count();

                        System.out.println("online: "+onlineFriendCount+"\nall: "+friendCount);

                    }

                    @Override
                    public void onRelationshipUpdate(Relationship relationship)
                    {
                        // for debugging
                        System.out.println("RelationshipExample.onRelationshipUpdate");
                        System.out.println("relationship = " + relationship);

                        // A relationship has changed -> update activity by calling onRelationshipRefresh manually
                        onRelationshipRefresh();
                    }

                });

                try(Core core = new Core(params)) {
                    // Run callbacks forever
                    SCC.RPCcore = core;

                    while(true) {
                        core.runCallbacks();
                        try {
                            // Sleep a bit to save CPU
                            Thread.sleep(16);
                        }
                        catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!SCCConfig.RPC && SCC.RPCon) {
                            core.activityManager().clearActivity();
                            SCC.RPCon = false;
                        } else if(SCCConfig.RPC) {
                            RPC.update(SCC.RPCcore);
                        }

                    }
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            System.err.println("Error downloading Discord SDK.");
            System.exit(-1);
        }

    }

    public static void update(Core core) {
        try(Activity activity = new Activity())  {
            activity.setDetails(Transformers.DiscordPlaceholder(SCCConfig.RPCLineOne));
            activity.setState(Transformers.DiscordPlaceholder(SCCConfig.RPCLineTwo));

            activity.timestamps().setStart(timestamp);

            activity.party().size().setMaxSize(4);
            activity.party().size().setCurrentSize(1);

            if (SCCConfig.BadSbeMode) {
                activity.assets().setLargeImage("nosbe");
            } else {
                activity.assets().setLargeImage("skyclienticon");
            }

            activity.assets().setLargeText(Transformers.DiscordPlaceholder(SCCConfig.RPCImgText));

            activity.party().setID(SCC.PartyID);
            activity.secrets().setJoinSecret("Secret");

            core.activityManager().updateActivity(activity);
            SCC.RPCon = true;
        }

    }

    public static String generateID() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 127;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
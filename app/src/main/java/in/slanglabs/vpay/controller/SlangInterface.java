package in.slanglabs.vpay.controller;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangBuddyOptions;
import in.slanglabs.platform.SlangEntity;
import in.slanglabs.platform.SlangIntent;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.SlangSession;
import in.slanglabs.platform.action.SlangAction;
import in.slanglabs.platform.action.SlangMultiStepIntentAction;
import in.slanglabs.platform.prompt.SlangMessage;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

/**
 * This class is responsible for managing the interface between Slang and the app, and for
 * dealing with all the Slang specific knowledge.
 */

public class SlangInterface {
    private static final String APP_ID = "5c7dd015d7104f438a8a109864e9fd05";
    private static final String API_KEY = "6ceb4b1743594dea94179274146a406b";
    private static final String[] EN_ASR_HINTS = new String[] {
            "send", "receive", "transfer", "give", "giving", "take", "taking", "credit", "debit"
    };

    public static final String INTENT_SEND = "send";

    public static final String ENTITY_RECEIVER = "receiver";
    public static final String ENTITY_SENDER = "sender";
    public static final String ENTITY_AMOUNT = "amount";
    public static final String ENTITY_NOTES = "notes";

    private static Application appContext;
    private static AppActionHandler appActionHandler;
    private static VPayBuddyListener sBuddyListener = new VPayBuddyListener();
    private static VPayActionHandler sActionHandler = new VPayActionHandler();
    private static boolean mLaunchedByAssistant;

    // The main entry point to SlangInterface
    public static void init(Application app, Set<String> customerNames, AppActionHandler callback) {
        appContext = app;
        appActionHandler = callback;

        try {
            SlangBuddyOptions options = new SlangBuddyOptions.Builder()
                    .setApplication(app)
                    .setBuddyId(APP_ID)
                    .setAPIKey(API_KEY)
                    .setListener(sBuddyListener)
                    .setIntentAction(sActionHandler)
                    .setRequestedLocales(new HashSet<Locale>() {
                        {
                            add(SlangLocale.LOCALE_ENGLISH_IN);
                            add(SlangLocale.LOCALE_HINDI_IN);
                        }
                    })
                    .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                    .enableEnhancedSpeechRecognition(true)
                    .setEnvironment(SlangBuddy.Environment.STAGING)
                    .build();
            SlangBuddy.initialize(options);
            sBuddyListener.setCustomerNameHints(customerNames);
            sActionHandler.setCustomerNames(customerNames);
        } catch (Exception e) {
            displayMessage("Error: " + e.getLocalizedMessage());
        }
    }

    public static void setCustomerNames(Set<String> customerNames) {
        sBuddyListener.setCustomerNameHints(customerNames);
        sActionHandler.setCustomerNames(customerNames);
    }

    public static void launchedByAssistant(boolean b) {
        mLaunchedByAssistant = b;
        // If Slang is initialized by now, then do the same thing we would have done at
        // init time
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SlangBuddy.isInitialized()) {
                    greetAndTalk();
                }
            }
        }, 1000);
    }

    private static Pair<String, String> getGreetingPrefix() {
        Calendar c = Calendar.getInstance();
        String greetingEn = "";
        String greetingHi = "नमस्कार, ";
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay >= 0 && timeOfDay < 12) {
            greetingEn = "Good Morning, ";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            greetingEn = "Good Afternoon, ";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            greetingEn = "Good Evening, ";
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            greetingEn = "Good Evening, ";
        }

        return new Pair(greetingEn, greetingHi);
    }

    private static void greetAndTalk() {
        if (SlangBuddy.isInitialized()) {
            Pair result = getGreetingPrefix();
            try {
                SlangBuddy.startConversation(getGreetingMessage(result.first.toString(), result.second.toString()), true);
            } catch (SlangBuddy.UninitializedUsageException e) {
                e.printStackTrace();
            } catch (SlangBuddy.SlangDisabledException e) {
                e.printStackTrace();
            }
        }
    }

    // The primary action handler for the VPay app
    public static class VPayActionHandler implements SlangMultiStepIntentAction, AppActionListener {
        private Set<String> mCustomerNames = new HashSet<>();
        private SlangEntity mCurrentEntity = null;

        @Override
        public SlangAction.Status action(SlangIntent slangIntent, SlangSession slangSession) {
            switch (slangIntent.getName()) {
                case INTENT_SEND:
                    slangSession.waitForActionCompletion();
                    appActionHandler.sendMoney(slangIntent, slangSession, this);
                    break;
            }
            try {
                setupGreeting(false);
            } catch (SlangBuddy.UninitializedUsageException ignored) {}
            return Status.SUCCESS;
        }

        @Override
        public void onIntentResolutionBegin(SlangIntent intent, SlangSession session) {}

        @Override
        public Status onEntityUnresolved(SlangEntity entity, SlangSession session) {
            return Status.SUCCESS;
        }

        @Override
        public Status onEntityResolved(SlangEntity entity, SlangSession session) {
            if (entity.getName().equals(ENTITY_RECEIVER) || entity.getName().equalsIgnoreCase(ENTITY_SENDER)) {
                if (mCustomerNames != null) {
                    List<String> matches = matchCustomerName(entity.getValue(), mCustomerNames);
                    if (matches.size() > 1 && appActionHandler != null) {
                        session.waitForActionCompletion();
                        mCurrentEntity = entity;
                        appActionHandler.resolveContact(matches, session, this);
                    } else {
                        entity.update(matches.get(0));
                    }
                }
            }
            return Status.SUCCESS;
        }

        @Override
        public void onIntentResolutionEnd(SlangIntent intent, SlangSession session) {}

        private void setCustomerNames(Set<String> customerNames) {
            mCustomerNames = customerNames;
        }

        @Override
        public void notifyContactResolution(String resolvedContactName, SlangSession session) {
            if (mCurrentEntity != null) {
                mCurrentEntity.update(resolvedContactName);
            }

            if (null != session) session.notifyActionCompleted(Status.SUCCESS);
        }
    }

    private static class VPayBuddyListener implements SlangBuddy.Listener {
        private Map<Locale, Set<String>> mASRHints;

        @Override
        public void onInitialized() {
            try {
                if (mASRHints != null) SlangBuddy.setSpeechRecognitionHints(mASRHints);
                setupGreeting(true);
                SlangBuddy.getBuiltinUI().disableLocaleSelection(true);
            } catch (SlangBuddy.UninitializedUsageException e) {
                displayMessage(e.getMessage());
            }
            appActionHandler.onVoiceInitialised();
        }

        @Override
        public void onInitializationFailed(final SlangBuddy.InitializationError e) {
            displayMessage("Slang initialization failed");
        }

        @Override
        public void onLocaleChanged(final Locale newLocale) {
            displayMessage("Locale changed: " + newLocale.getDisplayName());
            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(appContext);

            Intent localIntent = new Intent("localeChanged");
            localIntent.putExtra("localeBroadcast", newLocale.getLanguage());
            // Send local broadcast
            localBroadcastManager.sendBroadcast(localIntent);
        }

        @Override
        public void onLocaleChangeFailed(final Locale newLocale, final SlangBuddy.LocaleChangeError e) {
            displayMessage("Locale change failed for: " + newLocale.getDisplayName());
        }

        void setCustomerNameHints(Set<String> customerNames) {
            Map<Locale, Set<String>> asrHints = new HashMap<>();
            HashSet<String> enASRHints = new HashSet<>(Arrays.asList(EN_ASR_HINTS));
            if (customerNames != null) enASRHints.addAll(customerNames);
            asrHints.put(SlangLocale.LOCALE_ENGLISH_IN, enASRHints);
            asrHints.put(SlangLocale.LOCALE_HINDI_IN, enASRHints);

            mASRHints = asrHints;
            if (SlangBuddy.isInitialized()) {
                try {
                    SlangBuddy.setSpeechRecognitionHints(mASRHints);
                } catch (SlangBuddy.UninitializedUsageException e) {
                    displayMessage("Error: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public interface AppActionHandler {
        void resolveContact(List<String> customerNames, SlangSession session, AppActionListener handler);
        void sendMoney(
                SlangIntent slangIntent,
                SlangSession session,
                AppActionListener handler
        );
        void onVoiceInitialised();
    }

    public interface AppActionListener {
        void notifyContactResolution(String contactName, SlangSession session);
    }

    private static List<String> matchCustomerName(String name, Set<String> customerNames) {
        List<ExtractedResult> results = FuzzySearch.extractTop(name, customerNames, 5, 60);
        List<String> ret = new ArrayList<>(results.size());
        for (ExtractedResult result: results) {
            ret.add(result.getString());
        }
        if (ret.size() == 0) ret.add(name);
        return ret;
    }

    private static void displayMessage(final String message) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(appContext, message, Toast.LENGTH_LONG).show();
            }
        }, 10);
    }

    private static void setupGreeting(boolean isSpoken) throws SlangBuddy.UninitializedUsageException {
        Pair prefix = getGreetingPrefix();
        SlangBuddy.getGreetingMessage().overrideMessage(getGreetingMessage(prefix.first.toString(), prefix.second.toString()));
        SlangBuddy.getGreetingMessage().overrideIsSpoken(isSpoken);
    }

    private static SlangMessage getGreetingMessage(String prefixEn, String prefixHi) {
        HashMap<Locale, String> strings = new HashMap<>();
        prefixEn = (prefixEn == null) ? "" : prefixEn;
        prefixHi = (prefixHi == null) ? "" : prefixHi;
        strings.put(SlangLocale.LOCALE_ENGLISH_IN, prefixEn + "Welcome to VPay, whom do you want to send the money and how much?");
        strings.put(SlangLocale.LOCALE_HINDI_IN, prefixHi + "VPay में आपका स्वागत है, आप किसे और कितना पैसा भेजना चाहते हैं?");
        SlangMessage message = SlangMessage.create(strings);

        return message;
    }
}

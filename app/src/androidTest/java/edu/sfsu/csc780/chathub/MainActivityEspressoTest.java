/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.sfsu.csc780.chathub;

import android.content.Intent;
import android.os.SystemClock;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import edu.sfsu.csc780.chathub.ui.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)

public class MainActivityEspressoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);


    /**
     * This method sends a random messages, then clicks on star message.
     * Once it is marked as starred message, oot then goes to the starres message
     * activity and tests if the random message is present there or not.
     *
     */
    @Test
    public void verifyStarredMessage() {
        MainActivity activity = mActivityRule.launchActivity(new Intent());

        String randomMessage = getRandomMessage();

        onView(withId(R.id.messageEditText)).perform(typeText(randomMessage), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());

        // Included to give time for RecyclerView to load before selection.
        // (Not the best approach)
        SystemClock.sleep(3000);

        int lastPos = activity.mFirebaseAdapter.getItemCount() - 1;
        onView(withId(R.id.messageRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(lastPos, longClick()));

        onView(withId(R.id.messageRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(lastPos, click()));

        onView(withId(R.id.starMessage))
                .perform(click());

        // Start star message activity
        onView(withId(R.id.star_message)).perform(click());

        onView(withText(randomMessage))
                .check(matches(withText(randomMessage)));

    }

    public String getRandomMessage() {
        Random random = new Random();
        int randomNumber = random.nextInt();
        return "Random message: " + randomNumber;
    }
}

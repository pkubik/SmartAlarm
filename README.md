SmartAlarm
==========

An Android application prepared for a moble development course on WUT.

The application monitors the traffic and manages the alarm clock in order to
help the user in avoiding traffic jams. The application turns off the alarm
clock to allow the user to sleep in until the difficulties are over. Note
that not everyone can afford such behaviour!


Usage
-----

User must specified following options:
- Auto Dismiss Mode - whether to automatically dismiss the alarm
- Delay Ratio Threshold - typical / current travel time ratio, when it's exceeded
and the auto-dismiss mode is on, the alarm is dismissed
- Initial and Final checkpoints - a segment to monitor, if there are many paths
which allow to reach the final checkpoint from the initial checkpoint the
shortest are being considered
- Alarm Time - time of the alarm

How does it work?
-----------------

The application is launching a service at specified Alarm Time. The service
queries the Google Traffic API in order to measure delays on the roads connecting
the specified checkpoints. The results are always shown in a notification.
Additionally the alarm clock is launched if the delay has not exceeded the threshold
or the Auto Dismiss Mode is turned off.


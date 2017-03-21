SmartAlarm
==========

An Android application prepared for a moble development course on WUT.

The application monitors the traffic and manages the alarm clock in order to
help the user in avoiding traffic jams. The application turns off the alarm
clock to allow the user to sleep in until the difficulties are over. Note
that not everyone can afford such behaviour!


How does it work?
-----------------

An associated with the application service is launched everyday at 3:00 AM
in order to schedule a job to be performed just before the next alarm clock.
The job consists of checking the traffic at the specified area to decide
whether to affect the alarm clock (by disabling it or by adding the notification
about the traffic).

The behaviour executed by the service at the specified time can be also executed
from within the user interface.

Note that if multiple alarm clocks are set, only the first one is going to
be affected.

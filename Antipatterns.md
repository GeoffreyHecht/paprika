Back to [README.md](README.md)

# Antipatterns


## Object-Oriented

### BLOB Class
A Blob class, also known as God class, is a class with a large number of attributes and/or methods.
The Blob class handles a lot of responsibilities as compared to other classes that only hold data or execute
simple processes.
Attributes and methods of this kind of classes are related to different concepts and processes,
implying a very low cohesion. Blob classes are also often associated with numerous data classes increasing 
coupling, and thus hindering software maintainability.

### Swiss Army Knife
A Swiss Army Knife is a very complex class interface containing lots of methods.
This interface is often designed to handle a wide diversity of abstractions or to meet all possible needs in just 
one interface. This type of interfaces and the classes that implement them are hard to understand and maintain
because of the resulting complexity.

### Long method
Long Methods are implemented with much more lines of code than the average.
They are often very complex, and thus hard to understand and maintain. These methods can usually be split into
smaller methods to fix the problem.

### Complex Class
A Complex Class is a class containing complex methods. Again, these classes are hard to understand and maintain 
and need to be refactored. The class complexity can be calculated by summing the internal complexity of each of 
its methods. The internal complexity of a method can be calculated using McCabe’s Cyclomatic Complexity.


## Micro-optimizations

### Internal Getter/setter
An Internal Getter/Setter is an Android code smell that occurs when an attribute is accessed, within the declaring
class, through a getter (var = getField()) and/or a setter (setField(var)). However, this indirect access to the
attribute may decrease the performance of the app. The usage of Internal Getter/Setter is a common practice in OO
languages, like C++, C# or Java, because compilers or virtual machines can usually inline the access. However, in
Android, there is only a simple inlining and, consequently, the usage of a trivial getter or setter is often 
converted into a virtual method call, which makes the operation at least three times slower than a direct access.
This code smell can be corrected by accessing the attribute directly within a class and declaring the getter and 
setter methods in the public interface.

### Member Ignoring Method
In Android, when a method is not a constructor or does not access an object attribute, it is recommended to use a
static method to increase performance. The static method invocations are about 15%–20% faster than dynamic 
invocations. It is also considered as a good practice for readability since it ensures that calling the method 
will not alter the object state.

### Init on Draw
OnDraw routines are responsible for updating the GUI of Android apps. These routines are invoked each time the GUI
is refreshed (up to 60 times per second), and thus any extra computational work done in OnDraw is magnified by that
frequency. Moreover, a high rate of memory allocations may lead into high memory consumption and numerous calls to 
garbage collection activities. Thus, ideally, OnDraw routines should never contain init instructions to allocate 
memory (either new or calls to factory/constructor).


## Memory Management

### No Low Memory Resolver
When the Android system is running low on memory, the system calls the method onLowMemory() of every running 
activity. This method is responsible of trimming the memory usage of the activity. If this method is not 
implemented by the activity, the Android system automatically kills the process of the activity to free memory.
This may lead to an unexpected program termination.

### Leaking Inner Class
In Java, an inner class refers to a class definition contained within another class. Non-static inner classes and
anonymous classes automatically hold a reference to the outter (containing) class and require the outter class to 
be created, whereas static inner classes do not. This could provoke a memory leak in Android systems in those cases 
in which the inner class is kept alive longer than its container, by being referenced by an object outside the 
containing class.

### Unsuited LRU Cache Size
In Android, we can easily work with a cache to manage automatically most frequently used items through the Least 
Recently Used (LRU) API. LRU automatically deletes the least used items when it needs space for loading the new
ones to the main memory. However, in order to not raise an “OutOfMemory” exception, the size of the LRU cache
should be adjusted according to the actual device memory. Thus, it is not recommended to initialize a LRU cache
without checking the actual available memory through a call to the getMemoryClass method.

### Hashmap Usage
The Android framework provides ArrayMap and SimpleArrayMap as replacements from traditional Java HashMap. They are 
supposed to be more memory-efficient and to trigger less garbage collection with no significant difference on 
operations performance for maps containing up to hundreds of values. So, unless a complex map for a large set of
objects is required, the use of ArrayMap should be preferred over the usage of HashMap in Android apps. Therefore,
creating small HashMap instances is considered as a code smell.


## UI drawing

### UI Overdraw
UI Overdraw occurs when the hardware spends CPU cycles drawing pixels that does not end up showing the final image 
in the screen. This can be due to the superposition of other graphical layers or when non-updated pixels are 
redrawn even when it is not needed. On the one hand, the Android canvas.cliprect() API allows developers to define 
the drawable boundaries of a given view, and only the graphical components inside these boundaries will be drawn.
Every graphical component outside these boundaries will be discarded and not even sent to the graphical hardware. 
This allows the GPU to avoid coloring anything that is going to be clipped. On the other hand, the 
canvas.quickreject() API allows developers to avoid a given area of the graphical interface to be considered during 
the drawing phase. Not using any of those APIs in a given Android graphical interface may have negative effects on 
the observed performance of the app.

### Invalidate without Rect
In most cases, the Android system is able to automatically detect whether a change occurred in a view that needs 
to be refreshed. However, when creating some animations or using custom views, it might be necessary to force the 
refreshing of views. Android provides a way to force the drawing of views from the code by calling the methods 
invalidate() of the concerned views. This redrawing is executed in the main UI thread and can be an expensive 
operation, especially if invalidate() is called frequently. To reduce the drawing time, it is recommended to specify 
exactly which parts of the view need to be drawn again by using a Rect as argument when calling invalidate(). Thus,
the usage of invalidate() without Rect is considered as a code smell since it may hinder the performance of the app.

### Unsupported Hardware Acceleration
Android supports graphic hardware acceleration, meaning that the majority of the drawing operations use the GPU for
their execution. However, certain API operations (e.g., the drawPath method in android.graphics.Canvas) are 
not executed on the GPU, but on the CPU making the drawing phase much slower. These operations should be avoided
and replaced with hardware acceleration operations when possible.


## Thread blocking

### Heavy AsyncTask
In Android, the AsyncTask API allows developers to perform short background operations. However, three out of the 
four steps of AsyncTask are executed on the main UI thread and not in the background. Thus, these steps should not 
be time consuming or blocking operations to avoid: i) the GUI becoming unresponsive to user interactions or ii)
the ANR dialog to be shown. Thus, a class extending AsyncTask should never contain time consuming or blocking 
onPostExecute, onPreExecute, or onProgressUpdate methods.

### Heavy Service Start
Services in Android can perform heavy operations in background. However, Android services run in the main thread 
of their hosting process. By default, the service execution starts with a call to the OnStartCommand of the
service, which is run in the main UI thread. Thus, the OnStartCommand should never contain time consuming 
operations, otherwise it may cause the app to freeze or to display an ANR dialog.
Instead, when the service executes time consuming or asynchronous operations, a new thread should be created by
the method OnStartCommand to handle these operations outside the main UI thread.

### Heavy BroadcastReceiver
Android apps can use a broadcast receiver to manage broadcast communications with the system or other apps. 
However, the onReceive method of BroadCastReceiver runs in the main UI thread. Thus, if this method contains 
timeconsuming or blocking operations, it may also cause the app to freeze or to show an ANR dialog.


## Antipatterns from aDoctor
A description on some of the antipatterns can be found in the paper
[Lightweight Detection of Android-specific Code Smells: the aDoctor Project (pdf)](https://dibt.unimol.it/staff/fpalomba/documents/C18.pdf)

The concerned antipatterns are:
* Debuggable release,
* Durable wakelock,
* Public data,
* Rigid AlarmManager.


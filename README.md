Graffiti-codes
==============

Graffiti Codes is a project out of the MIT Media lab for the creation of QR-like gesture codes.

It is a work in progress, and currently more of a demo than a fully working service.

Areas for Major Improvement:
==============
- UI - differentiate URL's and plaintext, make it a bit more natural.
- Detection Algorithm - Currently naively based on number of vertices, lots more metrics to detect.
- Data preprocessing - move as much as possible to client to reduce server load.
- Convert database access to Motor from AsyncMongo. 


Running:
=============

Run the python server on your webserver `$ python run.py`, and the client (apk) on your android device.

(When running locally, be sure to edit the client code to hit your own server)

You should be able to figure out all dependencies from the Imports.py file.




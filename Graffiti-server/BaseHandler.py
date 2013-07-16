from App import *
class BaseHandler(tornado.web.RequestHandler):
    @property
    def db(self):
        if not hasattr(self, '_db'):
            self._db = asyncmongo.Client(pool_id='test_pool', host='127.0.0.1', port=27017, maxcached=10, maxconnections=50, dbname='graffiti')
        return self._db
    def date_handler(self, obj):
        # Handle date properly on decoding
        if isinstance(obj, datetime.datetime):
            return 'temp broken'
        if isinstance(obj, ObjectId):
            return str(obj)
        else:
            return obj
    def get_current_user(self):
        user_JSON = self.get_secure_cookie("user")
        if not user_JSON: return None
        return tornado.escape.json_decode(user_JSON)


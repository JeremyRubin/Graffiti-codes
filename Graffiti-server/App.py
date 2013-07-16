from Imports import *
#
from Processor import Processor
# Import all handlers
from BaseHandler import BaseHandler
from MainPage import MainPage
from Scribble import Scribble
class Application(tornado.web.Application):
    def __init__(self):
        handlers = [(r"/?", MainPage),
                    (r"/scribbles/?", Scribble)
                    ]
        settings = dict(cookie_secret="GENERATE YOUR OWN SECRET MORE SECURE THAN THIS",
                        login_url="/",
                        template_path=os.path.join(os.path.dirname(__file__), "templates"),
                        static_path=os.path.join(os.path.dirname(__file__), "static"),
                        xsrf_cookies=False,
                        debug=True,
                        xheaders=True,
                        autoescape=None,
                        )
        tornado.web.Application.__init__(self, handlers, **settings)

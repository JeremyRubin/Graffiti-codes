from App import *
class Scribble(BaseHandler):
    def get(self):
        # Don't do anything on get, but allow testing to see if on
        self.write("GET")
        self.finish()
    @tornado.web.asynchronous
    @tornado.gen.engine
    def post(self):
        stuff = self.get_argument("data")
        processor = Processor(stuff)
        try:
            x = processor.run()
            print x[0]
            if processor.msg:
                # add a message to the db, clears existing entries
                self.db.msgs.update({'verts':len(x[0])},{'$set':{'msg':processor.msg}}, upsert=True,callback=(yield tornado.gen.Callback('key')))
                response = yield tornado.gen.Wait('key')
                if response[1]['error']:
                    raise tornado.web.HTTPError(500)
                else:
                    self.write(processor.msg)
            else:
                self.db.msgs.find({'verts':len(x[0])},limit=1,callback=(yield tornado.gen.Callback('key')))
                response = yield tornado.gen.Wait('key')
                if response[1]['error']:
                    raise tornado.web.HTTPError(500)
                else:
                    print response
                    self.write(response[0][0][0]['msg'])
                
        except IndexError:
            self.write("http://rubinte.ch")
        self.finish()

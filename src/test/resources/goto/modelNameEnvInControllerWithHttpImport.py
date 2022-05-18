from odoo import http


class MyController(http.Controller):

    def my_method(self):
        existing_obj = http.request.env['e<caret>xisting']
        existing_obj.do_something()

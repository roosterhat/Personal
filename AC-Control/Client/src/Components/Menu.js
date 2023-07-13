import React from 'react'

class Menu extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            open: false
        }
    }

    componentDidMount() {
        this.setState({open: true})
    }

    render = () => {
        return (
            <div className="menu-container">
                <div className={"menu" + (this.state.open ? "" : " closed")}>
                    <button className="expand" onClick={() => this.setState({open: !this.state.open})}><i class="fa-solid fa-angles-right"></i></button>
                    <div className="menu-content">
                        {this.props.children}
                    </div>
                </div>
            </div>
        );
    }
}

export default Menu